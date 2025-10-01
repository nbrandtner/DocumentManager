create table if not exists documents (
    id uuid primary key,
    original_filename varchar(255) not null,
    content_type varchar(100) not null,
    size bigint not null,
    uploaded_at timestamptz not null
);
