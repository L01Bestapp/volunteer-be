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
                            <p style="font-size: 12px; color: #666;">Được bảo vệ bởi <b>Quang Thắng - Quang Đức</b></p>
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
                        color: #ffffff;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>lấy lại mật khẩu và mở khóa tài khoản của bạn</h1>
                    <p>Vui lòng nhấn vào nút bên dưới để đổi mật khẩu mới:</p>
                    <a class="verify-button" href="%s">Click vào đây để lấy lại mật khẩu</a>
                    <p style="color: #444; font-size: 14px;">Mã này sẽ hết hạn sau <b>30 phút</b>. Vui lòng không chia sẻ link với bất kỳ ai.</p>
                    <p style="color: #555; font-size: 12px;">Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email.</p>
                    <p style="font-size: 12px; color: #666;">Được bảo vệ bởi <b>Quang Thắng - Quang Đức</b></p>
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

