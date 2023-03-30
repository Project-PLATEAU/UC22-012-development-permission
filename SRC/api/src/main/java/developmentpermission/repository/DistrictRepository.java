package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import developmentpermission.entity.District;

/**
 * F_大字Repositoryクラス
 */
public interface DistrictRepository extends JpaRepository<District, String> {

	/**
	 * 町丁目名一覧取得
	 * 
	 * @return 町丁目名一覧
	 */
	@Query(value = "SELECT district_id, district_name, district_kana, disp_order, result_column1, result_column2, result_column3, result_column4, result_column5 FROM f_district ORDER BY disp_order ASC", nativeQuery = true)
	List<District> getDistinctList();

}
