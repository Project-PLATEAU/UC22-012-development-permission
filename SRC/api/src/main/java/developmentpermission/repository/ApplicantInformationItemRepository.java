package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.ApplicantInformationItem;

/**
 * M_申請者情報項目Repositoryインタフェース
 */
@Transactional
@Repository
public interface ApplicantInformationItemRepository extends JpaRepository<ApplicantInformationItem, String> {

	/**
	 * 申請者情報入力項目一覧取得
	 * 
	 * @return 申請者情報入力項目一覧
	 */
	@Query(value = "SELECT applicant_information_item_id, display_order, display_flag, require_flag, item_name, regex, mail_address, search_condition_flag FROM m_applicant_information_item ORDER BY display_order ASC", nativeQuery = true)
	List<ApplicantInformationItem> getApplicantItems();

}
