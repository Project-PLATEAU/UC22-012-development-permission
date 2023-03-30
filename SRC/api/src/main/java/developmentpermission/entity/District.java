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
 * F_大字Entityクラス
 */
@Entity
@Data
@Table(name = "f_district")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class District implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 大字ID */
	@Id
	@Column(name = "district_id")
	private String districtId;

	/** 大字名 */
	@Column(name = "district_name")
	private String districtName;

	/** 大字名かな */
	@Column(name = "district_kana")
	private String districtKana;
	
	/** 表示順 */
	@Column(name = "disp_order")
	private Integer dispOrder;
	
	/** 大字地番検索結果表示カラム */
	@Column(name = "result_column1")
	private String resultColumn1;
	
	/** 大字地番検索結果表示カラム */
	@Column(name = "result_column2")
	private String resultColumn2;
	
	/** 大字地番検索結果表示カラム */
	@Column(name = "result_column3")
	private String resultColumn3;
	
	/** 大字地番検索結果表示カラム */
	@Column(name = "result_column4")
	private String resultColumn4;
	
	/** 大字地番検索結果表示カラム */
	@Column(name = "result_column5")
	private String resultColumn5;

}
