-- iM Notes Minimal: Supabase schema for offline-first + online sync
-- Run this SQL in the Supabase SQL Editor after creating a Supabase project.
-- This file is safe to run more than once. It also repairs a partially-created
-- public.notes table from earlier schema runs.

create extension if not exists pgcrypto;

-- Public profile table linked to Supabase Auth users.
create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  display_name text,
  avatar_url text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

-- Online notes table.
create table if not exists public.notes (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  local_id integer,
  title text not null default '',
  content text not null default '',
  color_index integer not null default 0,
  is_pinned boolean not null default false,
  client_created_at timestamptz,
  client_updated_at timestamptz,
  deleted_at timestamptz,
  sync_version integer not null default 1,
  device_id text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

-- Repair/upgrade table if it already existed from a previous partial run.
alter table public.notes add column if not exists user_id uuid references auth.users(id) on delete cascade;
alter table public.notes add column if not exists local_id integer;
alter table public.notes add column if not exists title text not null default '';
alter table public.notes add column if not exists content text not null default '';
alter table public.notes add column if not exists color_index integer not null default 0;
alter table public.notes add column if not exists is_pinned boolean not null default false;
alter table public.notes add column if not exists client_created_at timestamptz;
alter table public.notes add column if not exists client_updated_at timestamptz;
alter table public.notes add column if not exists deleted_at timestamptz;
alter table public.notes add column if not exists sync_version integer not null default 1;
alter table public.notes add column if not exists device_id text;
alter table public.notes add column if not exists created_at timestamptz not null default now();
alter table public.notes add column if not exists updated_at timestamptz not null default now();

-- Keep the color range sane. Drop first so reruns do not fail.
alter table public.notes drop constraint if exists notes_color_index_range;
alter table public.notes add constraint notes_color_index_range check (color_index >= 0 and color_index <= 99);

-- Avoid duplicate remote rows for the same local note on the same device.
do $$
begin
  if not exists (
    select 1
    from pg_constraint
    where conname = 'notes_user_device_local_unique'
      and conrelid = 'public.notes'::regclass
  ) then
    alter table public.notes
      add constraint notes_user_device_local_unique unique (user_id, device_id, local_id);
  end if;
end $$;

create index if not exists notes_user_id_idx on public.notes(user_id);
create index if not exists notes_user_updated_idx on public.notes(user_id, updated_at desc);
create index if not exists notes_user_deleted_idx on public.notes(user_id, deleted_at);
create index if not exists notes_user_pinned_idx on public.notes(user_id, is_pinned desc, updated_at desc);

-- Generic updated_at trigger.
create or replace function public.set_updated_at()
returns trigger as $$
begin
  new.updated_at = now();
  return new;
end;
$$ language plpgsql;

drop trigger if exists profiles_set_updated_at on public.profiles;
create trigger profiles_set_updated_at
before update on public.profiles
for each row execute function public.set_updated_at();

drop trigger if exists notes_set_updated_at on public.notes;
create trigger notes_set_updated_at
before update on public.notes
for each row execute function public.set_updated_at();

-- Automatically create a profile row when a new auth user signs up.
create or replace function public.handle_new_user()
returns trigger as $$
begin
  insert into public.profiles (id, display_name, avatar_url)
  values (
    new.id,
    coalesce(new.raw_user_meta_data ->> 'display_name', new.raw_user_meta_data ->> 'name', split_part(new.email, '@', 1)),
    new.raw_user_meta_data ->> 'avatar_url'
  )
  on conflict (id) do nothing;

  return new;
end;
$$ language plpgsql security definer;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
after insert on auth.users
for each row execute function public.handle_new_user();

-- Row Level Security.
alter table public.profiles enable row level security;
alter table public.notes enable row level security;

-- Profiles: users can read and update only their own profile.
drop policy if exists "Profiles are readable by owner" on public.profiles;
create policy "Profiles are readable by owner"
on public.profiles
for select
to authenticated
using (auth.uid() = id);

drop policy if exists "Profiles are updatable by owner" on public.profiles;
create policy "Profiles are updatable by owner"
on public.profiles
for update
to authenticated
using (auth.uid() = id)
with check (auth.uid() = id);

-- Notes: users can only access their own notes.
drop policy if exists "Users can read own notes" on public.notes;
create policy "Users can read own notes"
on public.notes
for select
to authenticated
using (auth.uid() = user_id);

drop policy if exists "Users can insert own notes" on public.notes;
create policy "Users can insert own notes"
on public.notes
for insert
to authenticated
with check (auth.uid() = user_id);

drop policy if exists "Users can update own notes" on public.notes;
create policy "Users can update own notes"
on public.notes
for update
to authenticated
using (auth.uid() = user_id)
with check (auth.uid() = user_id);

drop policy if exists "Users can delete own notes" on public.notes;
create policy "Users can delete own notes"
on public.notes
for delete
to authenticated
using (auth.uid() = user_id);

-- Recommended app behavior:
-- 1. Guest users write to Room only.
-- 2. Logged-in users write to Room first, mark notes as PENDING, then upload to public.notes.
-- 3. When upload succeeds, save remote id into Room.remoteId and mark syncStatus = SYNCED.
-- 4. For deletes, set deleted_at instead of hard deleting so other devices can receive the deletion.
-- 5. Pull remote changes using user_id + updated_at, then merge into Room.
