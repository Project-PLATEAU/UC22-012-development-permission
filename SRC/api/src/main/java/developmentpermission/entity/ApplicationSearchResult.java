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
 * M_申請情報検索結果Entityクラス.
 */
@Entity
@Data
@Table(name = "m_application_search_result")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApplicationSearchResult implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請情報検索結果ID */
	@Id
	@Column(name = "application_search_result_id")
	private String applicationSearchResultId;

	/** 参照タイプ */
	@Column(name = "reference_type")
	private String referenceType;

	/** 表示カラム名 */
	@Column(name = "display_column_name")
	private String displayColumnName;

	/** 表示順 */
	@Column(name = "display_order")
	private int displayOrder;

	/** テーブル名 */
	@Column(name = "table_name")
	private String tableName;

	/** テーブルカラム名 */
	@Column(name = "table_column_name")
	private String tableColumnName;

	/** レスポンスキー */
	@Column(name = "response_key")
	private String responseKey;

	/** テーブル幅 */
	@Column(name = "table_width")
	private Float tableWidth;
}
