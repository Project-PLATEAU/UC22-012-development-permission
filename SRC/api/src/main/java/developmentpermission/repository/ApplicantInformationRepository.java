package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.ApplicantInformation;

/**
 * O_申請者情報Repositoryインタフェース
 */
@Transactional
@Repository
public interface ApplicantInformationRepository extends JpaRepository<ApplicantInformation, String> {

	/**
	 * 申請者情報取得
	 * 
	 * @param collationId 照合ID
	 * @param password    パスワード
	 * @return 申請者情報
	 */
	@Query(value = "SELECT application_id, applicant_id, item_1, item_2, item_3, item_4, item_5, item_6, item_7, item_8, item_9, item_10, mail_address, collation_id, password FROM o_applicant_information WHERE collation_id = :collationId AND password = :password ORDER BY application_id ASC", nativeQuery = true)
	List<ApplicantInformation> getApplicantList(@Param("collationId") String collationId,
			@Param("password") String password);

	/**
	 * 申請者情報取得
	 * 
	 * @param applicationId 申請ID
	 * @return 申請者情報
	 */
	@Query(value = "SELECT application_id, applicant_id, item_1, item_2, item_3, item_4, item_5, item_6, item_7, item_8, item_9, item_10, mail_address, collation_id, password FROM o_applicant_information WHERE application_id = :applicationId ORDER BY application_id ASC", nativeQuery = true)
	List<ApplicantInformation> getApplicantList(@Param("applicationId") Integer applicationId);
}
