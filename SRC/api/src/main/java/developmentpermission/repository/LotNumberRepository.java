package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.LotNumber;

/**
 * F_地番Repositoryインタフェース
 */
@Transactional
@Repository
public interface LotNumberRepository extends JpaRepository<LotNumber, Integer> {

	/**
	 * 地番情報取得
	 * 
	 * @param chibanId 地番ID
	 * @return ラベル一覧
	 */
	@Query(value = "SELECT chiban_id, district_id, chiban,result_column1,result_column2,result_column3,result_column4,result_column5 FROM f_lot_number WHERE chiban_id = :chibanId ORDER BY chiban_id ASC", nativeQuery = true)
	List<LotNumber> findByChibanId(@Param("chibanId") Integer chibanId);

}
