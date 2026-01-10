-- ========================================
-- INSERT SAMPLE DATA FOR DATABASE
-- Excluding: roles, users, students, organizations, users_roles
-- ========================================
-- Assumptions:
--   - user_id = 2: Student (Võ Quang Thắng - MSSV: 2213214)
--   - user_id = 3: Organization (Sun - APPROVED)
-- ========================================

-- 1. INSERT 10 ACTIVITIES
-- activity_status: UPCOMING, ONGOING, COMPLETED
-- registration_state: OPEN, FULL, CLOSED, COMPLETED
-- category: EDUCATION_SUPPORT, SOCIAL_SUPPORT, COMMUNITY_SERVICE, ENVIRONMENT, HEALTH_CAMPAIGN, EVENT_SUPPORT, FUNDRAISING, OTHER

INSERT INTO activities (
    actual_participants, approved_participants, current_participants, max_participants,
    pending_participants, the_number_of_ctxh_day, completed_at, create_at,
    end_date_time, organization_id, registration_deadline, registration_opens_at,
    start_date_time, activity_status, registration_state, category, title, address,
    short_description, description, image_url, requirements
) VALUES
(
    0, 0, 0, 50, 0, 2.0, NULL, '2026-01-01 10:00:00',
    '2026-02-15 17:00:00', 3, '2026-02-10 23:59:59', '2026-01-01 00:00:00',
    '2026-02-15 08:00:00', 'UPCOMING', 'OPEN', 'EDUCATION_SUPPORT',
    'Dạy học miễn phí cho trẻ em vùng cao',
    'Xã Tà Nung, Đà Lạt, Lâm Đồng',
    'Hỗ trợ giảng dạy môn Toán, Tiếng Việt cho học sinh tiểu học',
    'Chương trình tình nguyện dạy học cho trẻ em vùng cao, giúp các em tiếp cận kiến thức cơ bản, rèn luyện kỹ năng học tập.',
    'https://example.com/activity1.jpg',
    'Nhiệt tình, có kinh nghiệm dạy học, sức khỏe tốt'
),
(
    15, 12, 15, 30, 3, 1.5, NULL, '2026-01-02 10:00:00',
    '2026-02-20 16:00:00', 3, '2026-02-15 23:59:59', '2026-01-02 00:00:00',
    '2026-02-20 09:00:00', 'UPCOMING', 'OPEN', 'ENVIRONMENT',
    'Trồng cây xanh bảo vệ môi trường',
    'Công viên Gia Định, TP.HCM',
    'Trồng 500 cây xanh tại khu vực công viên',
    'Hoạt động nhằm tăng diện tích cây xanh, cải thiện môi trường sống cho cộng đồng.',
    'https://example.com/activity2.jpg',
    'Yêu thích hoạt động ngoài trời, không ngại nắng mưa'
),
(
    30, 30, 30, 30, 0, 1.0, NULL, '2026-01-03 10:00:00',
    '2026-02-25 15:00:00', 3, '2026-02-20 23:59:59', '2026-01-03 00:00:00',
    '2026-02-25 10:00:00', 'UPCOMING', 'FULL', 'COMMUNITY_SERVICE',
    'Hỗ trợ người già neo đơn',
    'Quận 1, TP.HCM',
    'Thăm hỏi, tặng quà cho người già neo đơn',
    'Chương trình mang đến sự quan tâm, chia sẻ với những người già không nơi nương tựa.',
    'https://example.com/activity3.jpg',
    'Tình nguyện viên có trách nhiệm, biết quan tâm người khác'
),
(
    NULL, 8, 10, 25, 2, 1.0, NULL, '2026-01-04 10:00:00',
    '2026-03-01 14:00:00', 3, '2026-02-25 23:59:59', '2026-01-04 00:00:00',
    '2026-03-01 08:00:00', 'UPCOMING', 'OPEN', 'HEALTH_CAMPAIGN',
    'Khám bệnh từ thiện cho người nghèo',
    'Huyện Nhà Bè, TP.HCM',
    'Tổ chức khám bệnh, phát thuốc miễn phí',
    'Đợt khám bệnh giúp người nghèo được tiếp cận dịch vụ y tế cơ bản.',
    'https://example.com/activity4.jpg',
    'Ưu tiên sinh viên Y khoa hoặc có kiến thức y tế'
),
(
    NULL, 20, 20, 40, 0, 2.5, NULL, '2026-01-05 10:00:00',
    '2026-03-10 18:00:00', 3, '2026-03-05 23:59:59', '2026-01-05 00:00:00',
    '2026-03-10 07:00:00', 'UPCOMING', 'CLOSED', 'EVENT_SUPPORT',
    'Hỗ trợ tổ chức Marathon từ thiện',
    'Khu đô thị Phú Mỹ Hưng, Quận 7, TP.HCM',
    'Hỗ trợ điều phối, phục vụ nước, hướng dẫn vận động viên',
    'Sự kiện Marathon quyên góp ủng hộ trẻ em khó khăn, cần tình nguyện viên hỗ trợ tổ chức.',
    'https://example.com/activity5.jpg',
    'Năng động, có tinh thần làm việc nhóm'
),
(
    25, 25, 25, 25, 0, 1.0, '2026-01-15 17:00:00', '2026-01-06 10:00:00',
    '2026-01-15 17:00:00', 3, '2026-01-10 23:59:59', '2026-01-06 00:00:00',
    '2026-01-15 08:00:00', 'COMPLETED', 'COMPLETED', 'SOCIAL_SUPPORT',
    'Tặng quà Tết cho người nghèo',
    'Quận 4, TP.HCM',
    'Phát 200 phần quà Tết cho hộ nghèo',
    'Chương trình mang Tết đến với người nghèo, chia sẻ niềm vui đầu xuân.',
    'https://example.com/activity6.jpg',
    'Tất cả sinh viên có thể tham gia'
),
(
    NULL, 5, 8, 20, 3, 1.5, NULL, '2026-01-07 10:00:00',
    '2026-03-20 16:00:00', 3, '2026-03-15 23:59:59', '2026-01-07 00:00:00',
    '2026-03-20 09:00:00', 'UPCOMING', 'OPEN', 'FUNDRAISING',
    'Gây quỹ hỗ trợ học sinh nghèo hiếu học',
    'Trường THPT Lê Quý Đôn, Quận 3, TP.HCM',
    'Tổ chức bán hàng handmade để gây quỹ',
    'Dự án gây quỹ giúp học sinh nghèo có điều kiện tiếp tục việc học.',
    'https://example.com/activity7.jpg',
    'Có kỹ năng giao tiếp, bán hàng'
),
(
    NULL, 10, 12, 35, 2, 2.0, NULL, '2026-01-08 10:00:00',
    '2026-03-25 17:00:00', 3, '2026-03-20 23:59:59', '2026-01-08 00:00:00',
    '2026-03-25 08:00:00', 'UPCOMING', 'OPEN', 'EDUCATION_SUPPORT',
    'Hướng nghiệp cho học sinh THPT',
    'Trường THPT Nguyễn Thị Minh Khai, TP.HCM',
    'Tư vấn nghề nghiệp, giới thiệu về các ngành đại học',
    'Chương trình giúp học sinh THPT định hướng nghề nghiệp phù hợp với năng lực.',
    'https://example.com/activity8.jpg',
    'Đang là sinh viên các trường đại học'
),
(
    NULL, 15, 18, 40, 3, 1.0, NULL, '2026-01-09 10:00:00',
    '2026-04-01 15:00:00', 3, '2026-03-25 23:59:59', '2026-01-09 00:00:00',
    '2026-04-01 09:00:00', 'UPCOMING', 'OPEN', 'ENVIRONMENT',
    'Dọn rác bãi biển Vũng Tàu',
    'Bãi biển Thùy Vân, Vũng Tàu',
    'Thu gom rác thải nhựa, làm sạch bãi biển',
    'Hoạt động bảo vệ môi trường biển, nâng cao ý thức cộng đồng.',
    'https://example.com/activity9.jpg',
    'Sức khỏe tốt, không ngại vất vả'
),
(
    NULL, 6, 10, 20, 4, 2.0, NULL, '2026-01-10 10:00:00',
    '2026-04-10 16:00:00', 3, '2026-04-05 23:59:59', '2026-01-10 00:00:00',
    '2026-04-10 08:00:00', 'UPCOMING', 'OPEN', 'OTHER',
    'Xây dựng thư viện cho trường học miền núi',
    'Huyện Đức Trọng, Lâm Đồng',
    'Sưu tầm sách, xây kệ sách, sắp xếp thư viện',
    'Dự án mang tri thức đến với học sinh vùng cao thông qua thư viện.',
    'https://example.com/activity10.jpg',
    'Chăm chỉ, cẩn thận, yêu thích sách'
);


-- 2. INSERT 10 ENROLLMENTS
-- Student ID: 2 (Võ Quang Thắng - MSSV: 2213214)
-- Status: PENDING, APPROVED, REJECTED, COMPLETED

INSERT INTO enrollments (
    is_completed, activity_id, applied_at, approved_at, approved_by,
    completed_at, create_at, enrollment_date, rejected_at, rejected_by,
    student_id, status
) VALUES
(
    false, 1, '2026-01-05 10:30:00', '2026-01-05 14:00:00', 3,
    NULL, '2026-01-05 10:30:00', '2026-01-05 10:30:00', NULL, NULL,
    2, 'APPROVED'
),
(
    false, 2, '2026-01-06 09:15:00', '2026-01-06 15:30:00', 3,
    NULL, '2026-01-06 09:15:00', '2026-01-06 09:15:00', NULL, NULL,
    2, 'APPROVED'
),
(
    false, 3, '2026-01-07 11:00:00', '2026-01-07 16:00:00', 3,
    NULL, '2026-01-07 11:00:00', '2026-01-07 11:00:00', NULL, NULL,
    2, 'APPROVED'
),
(
    false, 4, '2026-01-08 08:45:00', '2026-01-08 13:20:00', 3,
    NULL, '2026-01-08 08:45:00', '2026-01-08 08:45:00', NULL, NULL,
    2, 'APPROVED'
),
(
    false, 5, '2026-01-09 14:20:00', NULL, NULL,
    NULL, '2026-01-09 14:20:00', '2026-01-09 14:20:00', NULL, NULL,
    2, 'PENDING'
),
(
    true, 6, '2026-01-10 10:00:00', '2026-01-10 15:00:00', 3,
    '2026-01-15 17:00:00', '2026-01-10 10:00:00', '2026-01-10 10:00:00', NULL, NULL,
    2, 'COMPLETED'
),
(
    false, 7, '2026-01-11 09:30:00', '2026-01-11 14:30:00', 3,
    NULL, '2026-01-11 09:30:00', '2026-01-11 09:30:00', NULL, NULL,
    2, 'APPROVED'
),
(
    false, 8, '2026-01-12 11:45:00', '2026-01-12 16:00:00', 3,
    NULL, '2026-01-12 11:45:00', '2026-01-12 11:45:00', NULL, NULL,
    2, 'APPROVED'
),
(
    false, 9, '2026-01-13 08:00:00', NULL, NULL,
    NULL, '2026-01-13 08:00:00', '2026-01-13 08:00:00', NULL, NULL,
    2, 'PENDING'
),
(
    false, 10, '2026-01-14 10:20:00', NULL, NULL,
    NULL, '2026-01-14 10:20:00', '2026-01-14 10:20:00', '2026-01-14 15:00:00', 3,
    2, 'REJECTED'
);


-- 3. INSERT 10 TASKS
-- Distributed across activities 1-10
-- Task types: PREPARATION, MAIN_ACTIVITY, CLEANUP, LOGISTICS, COORDINATION, DOCUMENTATION, MEDIA, REGISTRATION, FEEDBACK, OTHER

INSERT INTO tasks (
    activity_id, create_at, task_type, name, description
) VALUES
(
    1, '2026-01-01 10:30:00', 'PREPARATION',
    'Chuẩn bị tài liệu giảng dạy',
    'Soạn giáo án, chuẩn bị bài tập cho học sinh tiểu học'
),
(
    2, '2026-01-02 10:30:00', 'LOGISTICS',
    'Vận chuyển cây giống',
    'Điều phối xe tải vận chuyển 500 cây giống đến công viên'
),
(
    3, '2026-01-03 10:30:00', 'COORDINATION',
    'Phối hợp với chính quyền địa phương',
    'Liên hệ UBND phường để lập danh sách người già neo đơn'
),
(
    4, '2026-01-04 10:30:00', 'MAIN_ACTIVITY',
    'Khám sức khỏe tổng quát',
    'Thực hiện khám lâm sàng, đo huyết áp, đường huyết cho bệnh nhân'
),
(
    5, '2026-01-05 10:30:00', 'REGISTRATION',
    'Đăng ký vận động viên',
    'Tiếp nhận hồ sơ đăng ký, cấp BIB number cho vận động viên'
),
(
    6, '2026-01-06 10:30:00', 'CLEANUP',
    'Dọn dẹp sau chương trình',
    'Thu dọn hiện trường, vệ sinh khu vực phát quà'
),
(
    7, '2026-01-07 10:30:00', 'MEDIA',
    'Quay phim, chụp ảnh sự kiện',
    'Ghi lại hình ảnh hoạt động bán hàng, phỏng vấn người ủng hộ'
),
(
    8, '2026-01-08 10:30:00', 'MAIN_ACTIVITY',
    'Thuyết trình giới thiệu ngành học',
    'Chia sẻ kinh nghiệm học tập, giới thiệu các ngành nghề'
),
(
    9, '2026-01-09 10:30:00', 'CLEANUP',
    'Thu gom và phân loại rác',
    'Nhặt rác, phân loại nhựa, kim loại, đưa về nơi xử lý'
),
(
    10, '2026-01-10 10:30:00', 'DOCUMENTATION',
    'Kiểm kê sách và lập danh mục',
    'Thống kê số lượng sách, phân loại theo thể loại, lập danh mục'
);


-- 4. INSERT 10 ATTENDANCES
-- For student_id = 2, various activities
-- Status: PRESENT, ABSENT

INSERT INTO attendances (
    activity_id, attendance_date, check_in_time, check_out_time,
    create_at, student_id, status
) VALUES
(
    6, '2026-01-15 08:00:00', '2026-01-15 07:55:00', '2026-01-15 17:05:00',
    '2026-01-15 08:00:00', 2, 'PRESENT'
),
(
    6, '2026-01-16 08:00:00', '2026-01-16 08:05:00', '2026-01-16 17:00:00',
    '2026-01-16 08:00:00', 2, 'PRESENT'
),
(
    1, '2026-02-15 08:00:00', '2026-02-15 07:50:00', '2026-02-15 17:10:00',
    '2026-02-15 08:00:00', 2, 'PRESENT'
),
(
    2, '2026-02-20 09:00:00', '2026-02-20 09:00:00', '2026-02-20 16:05:00',
    '2026-02-20 09:00:00', 2, 'PRESENT'
),
(
    3, '2026-02-25 10:00:00', '2026-02-25 09:55:00', '2026-02-25 15:00:00',
    '2026-02-25 10:00:00', 2, 'PRESENT'
),
(
    4, '2026-03-01 08:00:00', NULL, NULL,
    '2026-03-01 08:00:00', 2, 'ABSENT'
),
(
    7, '2026-03-20 09:00:00', '2026-03-20 09:10:00', '2026-03-20 16:00:00',
    '2026-03-20 09:00:00', 2, 'PRESENT'
),
(
    8, '2026-03-25 08:00:00', '2026-03-25 08:00:00', '2026-03-25 17:00:00',
    '2026-03-25 08:00:00', 2, 'PRESENT'
),
(
    9, '2026-04-01 09:00:00', NULL, NULL,
    '2026-04-01 09:00:00', 2, 'ABSENT'
),
(
    1, '2026-02-16 08:00:00', '2026-02-16 08:05:00', '2026-02-16 17:00:00',
    '2026-02-16 08:00:00', 2, 'PRESENT'
);


-- 5. INSERT 10 CERTIFICATES
-- For enrollment_id 1-10
-- Note: Certificates are typically issued only for COMPLETED enrollments
-- but for demo purposes, we'll create certificates for all enrollments

INSERT INTO certificates (
    ctxh_hours, is_revoked, activity_end_date, activity_id, activity_start_date,
    create_at, enrollment_id, issued_date, revoked_at, student_id,
    student_academic_year, student_mssv, certificate_code, representative_email,
    representative_name, student_faculty, student_name, activity_title,
    organization_name, revoke_reason
) VALUES
(
    8.0, false, '2026-01-15 17:00:00', 6, '2026-01-15 08:00:00',
    '2026-01-16 10:00:00', 6, '2026-01-16 10:00:00', NULL, 2,
    '2024-2025', '2213214', 'CERT-2026-00001', 'vqthang2004@gmail.com',
    'Nguyễn Văn A', 'Khoa Khoa học và Kỹ thuật Máy tính', 'Võ Quang Thắng',
    'Tặng quà Tết cho người nghèo', 'Sun', NULL
),
(
    16.0, false, '2026-02-15 17:00:00', 1, '2026-02-15 08:00:00',
    '2026-02-16 14:00:00', 1, '2026-02-16 14:00:00', NULL, 2,
    '2024-2025', '2213214', 'CERT-2026-00002', 'vqthang2004@gmail.com',
    'Nguyễn Văn A', 'Khoa Khoa học và Kỹ thuật Máy tính', 'Võ Quang Thắng',
    'Dạy học miễn phí cho trẻ em vùng cao', 'Sun', NULL
),
(
    12.0, false, '2026-02-20 16:00:00', 2, '2026-02-20 09:00:00',
    '2026-02-21 09:00:00', 2, '2026-02-21 09:00:00', NULL, 2,
    '2024-2025', '2213214', 'CERT-2026-00003', 'vqthang2004@gmail.com',
    'Nguyễn Văn A', 'Khoa Khoa học và Kỹ thuật Máy tính', 'Võ Quang Thắng',
    'Trồng cây xanh bảo vệ môi trường', 'Sun', NULL
),
(
    8.0, false, '2026-02-25 15:00:00', 3, '2026-02-25 10:00:00',
    '2026-02-26 11:00:00', 3, '2026-02-26 11:00:00', NULL, 2,
    '2024-2025', '2213214', 'CERT-2026-00004', 'vqthang2004@gmail.com',
    'Nguyễn Văn A', 'Khoa Khoa học và Kỹ thuật Máy tính', 'Võ Quang Thắng',
    'Hỗ trợ người già neo đơn', 'Sun', NULL
),
(
    8.0, true, '2026-03-01 14:00:00', 4, '2026-03-01 08:00:00',
    '2026-03-02 10:00:00', 4, '2026-03-02 10:00:00', '2026-03-05 10:00:00', 2,
    '2024-2025', '2213214', 'CERT-2026-00005', 'vqthang2004@gmail.com',
    'Nguyễn Văn A', 'Khoa Khoa học và Kỹ thuật Máy tính', 'Võ Quang Thắng',
    'Khám bệnh từ thiện cho người nghèo', 'Sun',
    'Sinh viên vắng mặt không phép trong ngày thứ 2'
),
(
    12.0, false, '2026-03-20 16:00:00', 7, '2026-03-20 09:00:00',
    '2026-03-21 14:00:00', 7, '2026-03-21 14:00:00', NULL, 2,
    '2024-2025', '2213214', 'CERT-2026-00006', 'vqthang2004@gmail.com',
    'Nguyễn Văn A', 'Khoa Khoa học và Kỹ thuật Máy tính', 'Võ Quang Thắng',
    'Gây quỹ hỗ trợ học sinh nghèo hiếu học', 'Sun', NULL
),
(
    16.0, false, '2026-03-25 17:00:00', 8, '2026-03-25 08:00:00',
    '2026-03-26 09:00:00', 8, '2026-03-26 09:00:00', NULL, 2,
    '2024-2025', '2213214', 'CERT-2026-00007', 'vqthang2004@gmail.com',
    'Nguyễn Văn A', 'Khoa Khoa học và Kỹ thuật Máy tính', 'Võ Quang Thắng',
    'Hướng nghiệp cho học sinh THPT', 'Sun', NULL
),
(
    8.0, false, '2026-04-01 15:00:00', 9, '2026-04-01 09:00:00',
    '2026-04-02 11:00:00', 9, '2026-04-02 11:00:00', NULL, 2,
    '2024-2025', '2213214', 'CERT-2026-00008', 'vqthang2004@gmail.com',
    'Nguyễn Văn A', 'Khoa Khoa học và Kỹ thuật Máy tính', 'Võ Quang Thắng',
    'Dọn rác bãi biển Vũng Tàu', 'Sun', NULL
),
(
    16.0, false, '2026-04-10 16:00:00', 10, '2026-04-10 08:00:00',
    '2026-04-11 10:00:00', 10, '2026-04-11 10:00:00', NULL, 2,
    '2024-2025', '2213214', 'CERT-2026-00009', 'vqthang2004@gmail.com',
    'Nguyễn Văn A', 'Khoa Khoa học và Kỹ thuật Máy tính', 'Võ Quang Thắng',
    'Xây dựng thư viện cho trường học miền núi', 'Sun', NULL
),
(
    20.0, false, '2026-03-10 18:00:00', 5, '2026-03-10 07:00:00',
    '2026-03-11 15:00:00', 5, '2026-03-11 15:00:00', NULL, 2,
    '2024-2025', '2213214', 'CERT-2026-00010', 'vqthang2004@gmail.com',
    'Nguyễn Văn A', 'Khoa Khoa học và Kỹ thuật Máy tính', 'Võ Quang Thắng',
    'Hỗ trợ tổ chức Marathon từ thiện', 'Sun', NULL
);


-- ========================================
-- END OF SCRIPT
-- ========================================
