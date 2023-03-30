package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.Label;
import developmentpermission.entity.key.LabelKey;

/**
 * M_ラベルRepositoryインタフェース
 */
@Transactional
@Repository
public interface LabelRepository extends JpaRepository<Label, LabelKey> {

	/**
	 * ラベル一覧取得
	 * 
	 * @param viewCode  画面コード
	 * @param labelType 種別
	 * @return ラベル一覧
	 */
	@Query(value = "SELECT view_code, label_id, label_key, label_type, label_text FROM m_label WHERE view_code = :viewCode AND label_type in ('0', :labelType) ORDER BY label_id ASC", nativeQuery = true)
	List<Label> findByViewCode(@Param("viewCode") String viewCode, @Param("labelType") String labelType);

}
