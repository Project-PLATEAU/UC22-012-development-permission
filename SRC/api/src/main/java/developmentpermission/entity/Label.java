package developmentpermission.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import developmentpermission.entity.key.LabelKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * M_ラベルEntityクラス
 */
@Entity
@Data
@Table(name = "m_label")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@IdClass(value=LabelKey.class)
public class Label implements Serializable {
	
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** ラベルID */
	@Id
	@Column(name = "label_id")
	private String labelId;

	/** 画面コード  */
	@Id
	@Column(name = "view_code")
	private String viewCode;

	/** ラベルキー */
	@Column(name = "label_key")
	private String labelKey;

	/** 種別 */
	@Column(name = "label_type")
	private String labelType;

	/** テキスト */
	@Column(name = "label_text")
	private String labelText;
}
