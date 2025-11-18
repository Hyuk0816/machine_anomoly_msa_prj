package dev.study.portal.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class BaseEntity {
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected Long id;

	@Column(name = "create_by", updatable = false)
	@CreatedBy
	protected String createBy;

	@CreationTimestamp
	@Column(name = "create_at", nullable = false, updatable = false)
	protected LocalDateTime createdAt;

	@Column(name = "update_by")
	@LastModifiedBy
	protected String updateBy;

	@UpdateTimestamp
	@Column(name = "update_at")
	protected LocalDateTime updateAt;
}
