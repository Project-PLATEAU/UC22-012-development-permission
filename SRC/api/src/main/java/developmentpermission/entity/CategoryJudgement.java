package developmentpermission.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * M_区分判定Entityクラス.
 *
 *
 */
@Entity
@Data
@Table(name = "m_category_judgement")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CategoryJudgement implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 判定項目ID */
	@Id
	@Column(name = "judgement_item_id")
	private String judgementItemId;

	/** 担当部署ID */
	@Column(name = "department_id")
	private String departmentId;

	/** 区分1 */
	@Column(name = "category_1")
	private String category1;

	/** 区分2 */
	@Column(name = "category_2")
	private String category2;

	/** 区分3 */
	@Column(name = "category_3")
	private String category3;

	/** 区分4 */
	@Column(name = "category_4")
	private String category4;

	/** 区分5 */
	@Column(name = "category_5")
	private String category5;

	/** 区分6 */
	@Column(name = "category_6")
	private String category6;

	/** 区分7 */
	@Column(name = "category_7")
	private String category7;

	/** 区分8 */
	@Column(name = "category_8")
	private String category8;

	/** 区分9 */
	@Column(name = "category_9")
	private String category9;

	/** 区分10 */
	@Column(name = "category_10")
	private String category10;

	/** GIS判定 */
	@Column(name = "gis_judgement")
	private String gisJudgement;

	/** バッファ */
	@Column(name = "buffer")
	private Double buffer;

	/** 重なり属性表示フラグ */
	@Column(name = "display_attribute_flag")
	private String displayAttributeFlag;

	/** 判定対象レイヤ */
	@Column(name = "judgement_layer")
	private String judgementLayer;

	/** タイトル */
	@Column(name = "title")
	private String title;

	/** 該当表示概要 */
	@Column(name = "applicable_summary")
	private String applicableSummary;

	/** 該当表示文言 */
	@Column(name = "applicable_description")
	private String applicableDescription;

	/** 非該当表示有無 */
	@Column(name = "non_applicable_display_flag", columnDefinition = "char(1)")
	private Boolean nonApplicableDisplayFlag;

	/** 非該当表示概要 */
	@Column(name = "non_applicable_summary")
	private String nonApplicableSummary;

	/** 非該当表示文言 */
	@Column(name = "non_applicable_description")
	private String nonApplicableDescription;

	/** テーブル名 */
	@Column(name = "table_name")
	private String tableName;

	/** フィールド名 */
	@Column(name = "field_name")
	private String fieldName;

	/** 判定レイヤ非該当時表示有無 */
	@Column(name = "non_applicable_layer_display_flag", columnDefinition = "char(1)")
	private Boolean nonApplicableLayerDisplayFlag;

	/** 同時表示レイヤ */
	@Column(name = "simultaneous_display_layer")
	private String simultaneousDisplayLayer;

	/** 同時表示レイヤ表示有無 */
	@Column(name = "simultaneous_display_layer_flag", columnDefinition = "char(1)")
	private Boolean simultaneousDisplayLayerFlag;

	/** 回答必須フラグ */
	@Column(name = "answer_require_flag", columnDefinition = "char(1)")
	private Boolean answerRequireFlag;

	/** デフォルト回答 */
	@Column(name = "default_answer")
	private String defaultAnswer;
	
	/** 編集可能フラグ */
	@Column(name = "answer_editable_flag", columnDefinition = "char(1)")
	private Boolean answerEditableFlag;
}
