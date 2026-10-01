create table members (
    id uuid primary key,
    first_name varchar(120) not null,
    last_name varchar(120) not null,
    email varchar(180) not null unique,
    phone varchar(60),
    country varchar(120),
    city varchar(120),
    full_address text,
    joined_at date not null,
    status varchar(40) not null,
    role varchar(40) not null,
    contribution_type varchar(40) not null,
    photo_url text,
    emergency_contact_name varchar(180),
    emergency_contact_phone varchar(80),
    total_meetings_chaired integer not null default 0,
    last_chaired_at timestamp with time zone,
    annual_authorized_absences integer not null default 0,
    annual_unauthorized_absences integer not null default 0,
    total_penalties_amount bigint not null default 0,
    unpaid_penalties_amount bigint not null default 0,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table users (
    id uuid primary key,
    member_id uuid not null references members(id),
    email varchar(180) not null unique,
    password_hash varchar(255) not null,
    role varchar(40) not null,
    enabled boolean not null default true,
    last_login_at timestamp with time zone,
    created_at timestamp with time zone not null
);

create table meetings (
    id uuid primary key,
    title varchar(255) not null,
    meeting_date date not null,
    planned_start_time time not null,
    planned_end_time time not null,
    online_link text,
    status varchar(40) not null,
    chairperson_id uuid references members(id),
    notes text,
    decisions_summary text,
    projects_summary text,
    created_at timestamp with time zone not null,
    started_at timestamp with time zone,
    completed_at timestamp with time zone,
    cancelled_at timestamp with time zone
);

create table meeting_chairperson_history (
    id uuid primary key,
    meeting_id uuid not null references meetings(id),
    member_id uuid not null references members(id),
    assignment_type varchar(40) not null,
    assigned_by uuid references members(id),
    reason text,
    assigned_at timestamp with time zone not null,
    completed_at timestamp with time zone
);

create table absence_requests (
    id uuid primary key,
    member_id uuid not null references members(id),
    meeting_id uuid not null references meetings(id),
    reason text not null,
    requested_at timestamp with time zone not null,
    hours_before_meeting bigint not null,
    status varchar(40) not null,
    validation_comment text,
    validated_by uuid references members(id),
    validated_at timestamp with time zone,
    exceptional_approval boolean not null default false
);

create table penalties (
    id uuid primary key,
    member_id uuid not null references members(id),
    meeting_id uuid references meetings(id),
    type varchar(40) not null,
    amount bigint not null,
    status varchar(40) not null,
    reason text not null,
    created_at timestamp with time zone not null,
    paid_at timestamp with time zone,
    validated_by uuid references members(id),
    cancellation_reason text
);

create table attendance_records (
    id uuid primary key,
    meeting_id uuid not null references meetings(id),
    member_id uuid not null references members(id),
    status varchar(50) not null,
    expected_start_at timestamp not null,
    arrival_at timestamp,
    delay_minutes integer not null default 0,
    penalty_id uuid references penalties(id),
    absence_request_id uuid references absence_requests(id),
    recorded_by uuid references members(id),
    created_at timestamp with time zone not null,
    unique (meeting_id, member_id)
);

create table tontine_cycles (
    id uuid primary key,
    cycle_month integer not null,
    cycle_year integer not null,
    beneficiary_id uuid references members(id),
    secondary_beneficiary_id uuid references members(id),
    status varchar(50) not null,
    expected_amount bigint not null default 0,
    collected_amount bigint not null default 0,
    transferred_amount bigint not null default 0,
    remaining_amount bigint not null default 0,
    opened_at timestamp with time zone,
    closed_at timestamp with time zone,
    comment text,
    unique (cycle_month, cycle_year)
);

create table contributions (
    id uuid primary key,
    cycle_id uuid not null references tontine_cycles(id),
    member_id uuid not null references members(id),
    expected_amount bigint not null,
    paid_amount bigint not null default 0,
    currency varchar(10) not null,
    status varchar(40) not null,
    paid_at timestamp with time zone,
    validated_by uuid references members(id),
    validated_at timestamp with time zone,
    unique (cycle_id, member_id)
);

create table contribution_payments (
    id uuid primary key,
    contribution_id uuid not null references contributions(id),
    amount bigint not null,
    method varchar(40) not null,
    transaction_reference varchar(255),
    proof_url text,
    paid_at timestamp with time zone not null,
    validated_by uuid references members(id),
    validated_at timestamp with time zone
);

create table beneficiary_confirmations (
    id uuid primary key,
    cycle_id uuid not null unique references tontine_cycles(id),
    beneficiary_id uuid not null references members(id),
    expected_amount bigint not null,
    received_amount bigint not null default 0,
    received_at timestamp with time zone,
    status varchar(50) not null,
    beneficiary_comment text,
    final_validated_by uuid references members(id),
    final_validated_at timestamp with time zone
);

create table projects (
    id uuid primary key,
    title varchar(255) not null,
    description text not null,
    responsible_id uuid references members(id),
    status varchar(40) not null,
    estimated_budget bigint not null default 0,
    real_budget bigint not null default 0,
    created_at timestamp with time zone not null,
    start_date date,
    end_date date
);

create table decisions (
    id uuid primary key,
    meeting_id uuid references meetings(id),
    project_id uuid references projects(id),
    title varchar(255) not null,
    description text not null,
    responsible_id uuid references members(id),
    due_date date,
    status varchar(40) not null,
    priority varchar(40) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table charity_fund_movements (
    id uuid primary key,
    member_id uuid references members(id),
    penalty_id uuid references penalties(id),
    project_id uuid references projects(id),
    type varchar(50) not null,
    amount bigint not null,
    description text not null,
    movement_date timestamp with time zone not null,
    created_by uuid references members(id)
);

create table notifications (
    id uuid primary key,
    recipient_id uuid not null references members(id),
    type varchar(80) not null,
    channel varchar(40) not null,
    title varchar(255) not null,
    message text not null,
    status varchar(40) not null,
    payload_json text,
    created_at timestamp with time zone not null,
    read_at timestamp with time zone
);

create table audit_logs (
    id uuid primary key,
    actor_id uuid references members(id),
    action varchar(80) not null,
    entity_type varchar(120) not null,
    entity_id varchar(120) not null,
    old_value_json text,
    new_value_json text,
    reason text,
    created_at timestamp with time zone not null
);

create index idx_members_status on members(status);
create index idx_meetings_date on meetings(meeting_date);
create index idx_attendance_member on attendance_records(member_id);
create index idx_contributions_cycle on contributions(cycle_id);
create index idx_penalties_member on penalties(member_id);
create index idx_notifications_recipient on notifications(recipient_id);
