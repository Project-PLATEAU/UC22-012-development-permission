package developmentpermission.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * F_地番、F_大字Entityクラス
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LotNumberAndDistrict implements Serializable {

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

	/** 経度 */
	@Column(name = "lon")
	private Double lon;

	/** 緯度 */
	@Column(name = "lat")
	private Double lat;

	/** 最小経度 */
	@Column(name = "minlon")
	private Double minlon;

	/** 最小緯度 */
	@Column(name = "minlat")
	private Double minlat;
	
	/** 最大経度 */
	@Column(name = "maxlon")
	private Double maxlon;

	/** 最大緯度 */
	@Column(name = "maxlat")
	private Double maxlat;

	/** 大字 大字名 */
	@Column(name = "ooaza_district_name")
	private String ooazaDistrictName;

	/** 大字 大字名かな */
	@Column(name = "ooaza_district_kana")
	private String ooazaDistrictKana;
	
	/** 申請 ステータス */
	@Column(name = "status")
	private String status;
	
	/** 申請 申請ID */
	@Column(name = "application_id")
	private Integer applicationId;
	
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
	
	/** 大字地番検索結果表示カラム */
	@Column(name = "ooaza_result_column1")
	private String ooazaResultColumn1;
	
	/** 大字地番検索結果表示カラム */
	@Column(name = "ooaza_result_column2")
	private String ooazaResultColumn2;
	
	/** 大字地番検索結果表示カラム */
	@Column(name = "ooaza_result_column3")
	private String ooazaResultColumn3;
	
	/** 大字地番検索結果表示カラム */
	@Column(name = "ooaza_result_column4")
	private String ooazaResultColumn4;
	
	/** 大字地番検索結果表示カラム */
	@Column(name = "ooaza_result_column5")
	private String ooazaResultColumn5;

}
