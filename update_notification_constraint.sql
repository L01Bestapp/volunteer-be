-- SQL Script to update notifications_type_check constraint
-- This adds the new ENROLLMENT_CREATED notification type

-- Step 1: Drop the existing constraint
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check;

-- Step 2: Create new constraint with ENROLLMENT_CREATED added
ALTER TABLE notifications ADD CONSTRAINT notifications_type_check
    CHECK (type IN (
        'REMINDER',
        'ATTENDANCE_COMPLETED',
        'ENROLLMENT_APPROVED',
        'ENROLLMENT_REJECTED',
        'ENROLLMENT_CREATED',
        'ACTIVITY_UPDATED',
        'ACTIVITY_CANCELLED',
        'GENERAL'
    ));

-- Verify the constraint was created successfully
SELECT conname, contype, pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conrelid = 'notifications'::regclass
  AND conname = 'notifications_type_check';
