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
 * M_地番検索結果定義Entityクラス
 */
@Entity
@Data
@Table(name = "m_lot_number_search_result_definition")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LotNumberSearchResultDefinition implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 地番検索結果定義ID */
	@Id
	@Column(name = "lot_number_search_definition_id")
	private String lotNumberSearchDefinitionId;

	/** 表示順 */
	@Column(name = "display_order")
	private Integer displayOrder;

	/** テーブル種別 */
	@Column(name = "table_type")
	private String tableType;

	/** 表示カラム名 */
	@Column(name = "display_column_name")
	private String displayColumnName;

	/** テーブルカラム名 */
	@Column(name = "table_column_name")
	private String tableColumnName;

	/** テーブル幅 */
	@Column(name = "table_width")
	private Float tableWidth;

	/** レスポンスキー */
	@Column(name = "response_key")
	private String responseKey;

}
