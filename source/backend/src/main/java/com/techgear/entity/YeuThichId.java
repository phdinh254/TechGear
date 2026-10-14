package com.techgear.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** Khóa chính kép của bảng yeu_thich. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class YeuThichId implements Serializable {

    @Column(name = "nguoi_dung_id")
    private Long nguoiDungId;

    @Column(name = "san_pham_id")
    private Long sanPhamId;
}
