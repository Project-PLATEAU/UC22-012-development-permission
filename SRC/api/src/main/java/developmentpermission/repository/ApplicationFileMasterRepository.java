package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.ApplicationFileMaster;
import developmentpermission.entity.key.ApplicationFileKey;

/**
 * M_申請ファイルRepositoryインタフェース
 */
@Transactional
@Repository
public interface ApplicationFileMasterRepository extends JpaRepository<ApplicationFileMaster, ApplicationFileKey> {

	/**
	 * 申請ファイル一覧取得
	 * 
	 * @param judgementItemIdList 判定項目IDリスト
	 * @return 申請ファイル一覧
	 */
	@Query(value = "SELECT application_file_id, require_flag, upload_file_name,'' as judgement_item_id FROM m_application_file WHERE judgement_item_id IN (:judgementItemId) GROUP BY application_file_id, require_flag, upload_file_name ORDER BY application_file_id", nativeQuery = true)
	List<ApplicationFileMaster> getApplicationFiles(@Param("judgementItemId") List<String> judgementItemIdList);

	/**
	 * 申請ファイル一覧取得
	 * 
	 * @param applicationFileId 申請ファイルID
	 * @return 申請ファイル一覧
	 */
	@Query(value = "SELECT application_file_id, judgement_item_id, require_flag, upload_file_name FROM m_application_file WHERE application_file_id = :applicationFileId ORDER BY application_file_id", nativeQuery = true)
	List<ApplicationFileMaster> getApplicationFile(@Param("applicationFileId") String applicationFileId);

}
