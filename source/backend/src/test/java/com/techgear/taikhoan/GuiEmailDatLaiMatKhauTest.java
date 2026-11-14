package com.techgear.taikhoan;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class GuiEmailDatLaiMatKhauTest {

    final JavaMailSender mailSender = mock(JavaMailSender.class);
    final YeuCauDatLaiMatKhau yeuCau = new YeuCauDatLaiMatKhau("an@vi-du.vn", "Nguyễn Văn An",
            "http://localhost:5173/dat-lai-mat-khau?ma=abc");

    @Test
    void chuaCauHinhSmtpThiKhongGui() {
        new GuiEmailDatLaiMatKhau(mailSender, "", "TechGear <no-reply@techgear.vn>").gui(yeuCau);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void daCauHinhThiGuiEmailCoLienKet() {
        new GuiEmailDatLaiMatKhau(mailSender, "shop@gmail.com", "TechGear <no-reply@techgear.vn>").gui(yeuCau);
        ArgumentCaptor<SimpleMailMessage> thu = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(thu.capture());
        assertArrayEquals(new String[]{"an@vi-du.vn"}, thu.getValue().getTo());
        assertEquals("TechGear <no-reply@techgear.vn>", thu.getValue().getFrom());
        assertTrue(thu.getValue().getText().contains(yeuCau.lienKet()));
        assertTrue(thu.getValue().getText().contains("15 phút"));
    }

    @Test
    void loiSmtpChiGhiLogKhongNem() {
        doThrow(new MailSendException("SMTP lỗi")).when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> new GuiEmailDatLaiMatKhau(mailSender, "shop@gmail.com", "x@y.vn").gui(yeuCau));
    }
}
