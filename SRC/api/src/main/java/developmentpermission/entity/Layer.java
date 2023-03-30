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
 * M_レイヤEntityクラス.
 *
 *
 */
@Entity
@Data
@Table(name = "m_layer")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Layer implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** レイヤID */
	@Id
	@Column(name = "layer_id")
	private String layerId;

	/** レイヤ種別 */
	@Column(name = "layer_type", columnDefinition = "char(1)")
	private Boolean layerType;

	/** レイヤ名 */
	@Column(name = "layer_name")
	private String layerName;

	/** テーブル名 */
	@Column(name = "table_name")
	private String tableName;

	/** レイヤコード */
	@Column(name = "layer_code")
	private String layerCode;

	/** レイヤクエリ */
	@Column(name = "layer_query")
	private String layerQuery;

	/** クエリ必須フラグ */
	@Column(name = "query_require_flag", columnDefinition = "char(1)")
	private Boolean queryRequireFlag;
}
