package developmentpermission.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

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
@Table(name = "f_lot_number")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LotNumber implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 地番ID */
	@Id
	@Column(name = "chiban_id")
	private Integer chibanId;

	/** 大字ID */
	@Column(name = "district_id")
	private String districtId;

	/** 地番 */
	@Column(name = "chiban")
	private String chiban;

	/** 地番検索結果表示カラム */
	@Column(name = "result_column1")
	private String resultColumn1;
	
	/** 地番検索結果表示カラム */
	@Column(name = "result_column2")
	private String resultColumn2;
	
	/** 地番検索結果表示カラム */
	@Column(name = "result_column3")
	private String resultColumn3;
	
	/** 地番検索結果表示カラム */
	@Column(name = "result_column4")
	private String resultColumn4;
	
	/** 地番検索結果表示カラム */
	@Column(name = "result_column5")
	private String resultColumn5;

}
