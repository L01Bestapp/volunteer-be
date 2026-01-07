package com.ctxh.volunteer.module.auth.enums;

import lombok.Getter;

@Getter
public enum EmailTemplates {
    VERIFY_EMAIL_TEMPLATE(
            "Xác nhận đăng ký tài khoản của bạn",
            """
                    <html>
                    <head>
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                background-color: #f4f4f4;
                                padding: 20px;
                            }
                            .container {
                                max-width: 600px;
                                margin: 0 auto;
                                background-color: #ffffff;
                                padding: 30px;
                                border-radius: 10px;
                                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
                                text-align: center;
                            }
                            h1 {
                                color: #333333;
                            }
                            p {
                                font-size: 16px;
                                color: #555555;
                            }
                            .verify-button {
                                display: inline-block;
                                margin-top: 20px;
                                padding: 12px 24px;
                                background-color: #007bff;
                                color: #ffffff !important;
                                text-decoration: none;
                                font-weight: bold;
                                border-radius: 5px;
                                font-size: 16px;
                            }
                            .verify-button:hover {
                                background-color: #0056b3;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <h1>Xác minh địa chỉ email của bạn</h1>
                            <p>Cảm ơn bạn đã đăng ký tài khoản. Vui lòng nhấn vào nút bên dưới để xác minh địa chỉ email:</p>
                            <a class="verify-button" href="%s">Click vào đây để xác minh email</a>
                            <p style="color: #444; font-size: 14px;">Mã này sẽ hết hạn sau <b>1 ngày (24 giờ)</b>. Vui lòng không chia sẻ link với bất kỳ ai.</p>
                            <p style="color: #555; font-size: 12px;">Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email.</p>
                            <p style="font-size: 12px; color: #666;">Được bảo vệ bởi <b>Uni Volunteer</b></p>
                        </div>
                    </body>
                    </html>
                    """
    ),
    VERIFY_RESET_PASSWORD_TEMPLATE(
            "Xác nhận lấy lại mật khẩu và mở khóa tài khoản của bạn",
            """
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                background-color: #f4f4f4;
                                padding: 20px;
                                margin: 0;
                            }
                            .container {
                                max-width: 600px;
                                margin: 0 auto;
                                background-color: #ffffff;
                                padding: 30px;
                                border-radius: 10px;
                                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
                                text-align: center;
                            }
                            h1 {
                                color: #333333;
                                font-size: 24px;
                                margin-bottom: 20px;
                            }
                            p {
                                font-size: 16px;
                                color: #555555;
                                line-height: 1.5;
                                margin-bottom: 10px;
                            }
                            /* Class mới cho mã OTP */
                            .otp-code {
                                display: inline-block;
                                margin: 20px 0;
                                padding: 15px 30px;
                                background-color: #f8f9fa; /* Màu nền nhẹ cho mã */
                                color: #007bff; /* Màu chữ chính (giống màu nút cũ) */
                                font-size: 32px;
                                font-weight: bold;
                                letter-spacing: 10px; /* Khoảng cách giữa các số */
                                border-radius: 8px;
                                border: 2px dashed #007bff; /* Viền nét đứt tạo điểm nhấn */
                                font-family: 'Courier New', Courier, monospace; /* Phông chữ monospace cho số */
                            }
                            .footer-note {
                                font-size: 12px;
                                color: #666;
                                margin-top: 30px;
                                border-top: 1px solid #eee;
                                padding-top: 20px;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <h1>Lấy lại mật khẩu</h1>
                            <p>Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>
                            <p>Vui lòng sử dụng mã xác minh 6 số bên dưới để tiếp tục:</p>
                    
                            <div class="otp-code">
                                %s
                            </div>
                            <p style="color: #444; font-size: 14px;">Mã này sẽ hết hạn sau <b>30 phút</b>. Vui lòng không chia sẻ mã này với bất kỳ ai.</p>
                            <p style="color: #555; font-size: 12px; margin-top: 10px;">Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email.</p>
                    
                            <div class="footer-note">
                                Được bảo vệ bởi <b>Uni Volunteer</b>
                            </div>
                        </div>
                    </body>
                    </html>
            """
    );

    private final String subject;
    private final String content;


    EmailTemplates(String subject, String content) {
        this.subject = subject;
        this.content = content;
    }

    public String formatContent(Object... args) {
        return String.format(this.content, args);
    }
}

