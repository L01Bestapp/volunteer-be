package com.ctxh.volunteer.module.auth.enums;

import lombok.Getter;

@Getter
public enum EmailTemplates {
    VERIFY_EMAIL_TEMPLATE(
            "Chào mừng đến với Univolunteer - Xác nhận tài khoản",
            """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Verify Email</title>
                <style>
                    body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; -webkit-font-smoothing: antialiased; }
                    .container { width: 100%%; max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); }
                    .header { background-color: #D81B60; padding: 30px 20px; text-align: center; }
                    .header h1 { color: #ffffff; margin: 0; font-size: 24px; font-weight: 600; letter-spacing: 1px; }
                    .content { padding: 40px 30px; text-align: center; color: #333333; }
                    .welcome-icon { font-size: 50px; margin-bottom: 20px; }
                    h2 { color: #333333; font-size: 22px; margin-bottom: 10px; font-weight: 700; }
                    p { color: #666666; font-size: 16px; line-height: 1.6; margin-bottom: 30px; }
                    .btn-verify { display: inline-block; padding: 15px 35px; background-color: #D81B60; color: #ffffff !important; text-decoration: none; border-radius: 50px; font-weight: bold; font-size: 16px; transition: background-color 0.3s; box-shadow: 0 4px 6px rgba(216, 27, 96, 0.3); }
                    .btn-verify:hover { background-color: #ad1457; }
                    .footer { background-color: #f9f9f9; padding: 20px; text-align: center; border-top: 1px solid #eeeeee; font-size: 12px; color: #999999; }
                    .expire-note { font-size: 13px; color: #888; margin-top: 25px; font-style: italic; }
                </style>
            </head>
            <body>
                <div style="padding: 40px 0;">
                    <div class="container">
                        <div class="header">
                            <h1>UNIVOLUNTEER</h1>
                        </div>
                        <div class="content">
                            <div class="welcome-icon">👋</div>
                            <h2>Xác thực địa chỉ email</h2>
                            <p>Cảm ơn bạn đã tham gia cộng đồng tình nguyện Univolunteer. Để bắt đầu hành trình ý nghĩa này, vui lòng xác nhận email của bạn bằng cách nhấn vào nút bên dưới.</p>
                            
                            <a href="%s" class="btn-verify">Xác thực tài khoản ngay</a>
                            
                            <p class="expire-note">Liên kết này sẽ hết hạn sau <b>24 giờ</b>.</p>
                        </div>
                        <div class="footer">
                            <p>Bạn nhận được email này vì đã đăng ký tài khoản tại Univolunteer.</p>
                            <p>© 2025 Univolunteer Team. All rights reserved.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """
    ),

    VERIFY_RESET_PASSWORD_TEMPLATE(
            "Yêu cầu đặt lại mật khẩu - Univolunteer",
            """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Reset Password</title>
                <style>
                    /* Reset CSS cơ bản */
                    body, table, td, a { -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%; }
                    table, td { mso-table-lspace: 0pt; mso-table-rspace: 0pt; }
                    img { -ms-interpolation-mode: bicubic; }
                    
                    body { margin: 0; padding: 0; width: 100%% !important; height: 100%% !important; background-color: #f4f4f4; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; }
                    
                    /* Style cho nội dung */
                    .otp-box { 
                        background-color: #f8f9fa; 
                        border: 2px dashed #D81B60; 
                        border-radius: 8px; 
                        padding: 15px; 
                        margin: 30px 0; 
                        text-align: center; 
                        display: block; /* Quan trọng để căn giữa trên một số client */
                    }
                    .otp-code { 
                        font-family: 'Courier New', Courier, monospace; 
                        font-size: 36px; 
                        font-weight: bold; 
                        color: #D81B60; 
                        letter-spacing: 8px; 
                    }
                </style>
            </head>
            <body style="background-color: #f4f4f4; margin: 0; padding: 0;">
                <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #f4f4f4;">
                    <tr>
                        <td align="center" style="padding: 40px 10px;">
                            
                            <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);">
                                
                                <tr>
                                    <td align="center" style="background-color: #ffffff; padding: 30px 20px; border-bottom: 3px solid #D81B60; text-align: center;">
                                        <h1 style="color: #D81B60; margin: 0; font-size: 26px; font-weight: 700; text-transform: uppercase; text-align: center; line-height: 1.2;">
                                            UNIVOLUNTEER
                                        </h1>
                                    </td>
                                </tr>
                                
                                <tr>
                                    <td style="padding: 40px 30px; text-align: center;">
                                        
                                        <table border="0" cellpadding="0" cellspacing="0" width="100%%">
                                            <tr>
                                                <td align="center">
                                                    <div style="width: 60px; height: 60px; background-color: #fce4ec; border-radius: 50%%; display: inline-block; line-height: 60px;">
                                                        <span style="font-size: 30px;">🔒</span>
                                                    </div>
                                                </td>
                                            </tr>
                                        </table>
    
                                        <h2 style="color: #333333; margin-top: 20px; font-size: 22px;">Đặt lại mật khẩu</h2>
                                        <p style="color: #666666; font-size: 16px; line-height: 1.6;">Chúng tôi nhận được yêu cầu khôi phục mật khẩu cho tài khoản của bạn. Đây là mã xác minh (OTP) của bạn:</p>
                                        
                                        <table border="0" cellpadding="0" cellspacing="0" width="100%%">
                                            <tr>
                                                <td align="center">
                                                    <div class="otp-box">
                                                        <span class="otp-code">%s</span>
                                                    </div>
                                                </td>
                                            </tr>
                                        </table>
                                        
                                        <p style="color: #666666; margin-top: 20px;">Mã này sẽ hết hạn sau <b>60 phút</b>.</p>
                                        
                                        <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #fff3cd; border-radius: 4px; margin-top: 20px;">
                                            <tr>
                                                <td style="padding: 15px; text-align: left; color: #856404; font-size: 13px;">
                                                    <strong>⚠️ Lưu ý:</strong> Tuyệt đối không chia sẻ mã này với bất kỳ ai, kể cả nhân viên hỗ trợ của Univolunteer.
                                                </td>
                                            </tr>
                                        </table>
                                        
                                        <p style="margin-top: 30px; font-size: 14px; color: #888;">Nếu bạn không yêu cầu thay đổi mật khẩu, hãy bỏ qua email này.</p>
                                    </td>
                                </tr>
                                
                                <tr>
                                    <td style="background-color: #f9f9f9; padding: 20px; text-align: center; font-size: 12px; color: #999999; border-top: 1px solid #eeeeee;">
                                        <p style="margin: 0;">© 2025 Univolunteer Team. Secure System.</p>
                                    </td>
                                </tr>
                            </table>
                            
                        </td>
                    </tr>
                </table>
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