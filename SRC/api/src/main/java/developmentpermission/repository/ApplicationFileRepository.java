package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.ApplicationFile;
import developmentpermission.entity.key.ApplicationFileKey;

/**
 * O_申請ファイルRepositoryインタフェース
 */
@Transactional
@Repository
public interface ApplicationFileRepository extends JpaRepository<ApplicationFile, ApplicationFileKey> {

	/**
	 * 申請ファイル一覧取得
	 * 
	 * @param judgementItemIdList 申請ファイルID
	 * @return 申請ファイル一覧
	 */
	@Query(value = "SELECT file_id, application_id, application_file_id, upload_file_name, file_path FROM o_application_file WHERE application_id = :applicationId AND application_file_id = :applicationFileId ORDER BY file_id ASC", nativeQuery = true)
	List<ApplicationFile> getApplicationFiles(@Param("applicationFileId") String applicationFileId,@Param("applicationId") Integer applicationId);

	/**
	 * 申請ファイル一覧取得
	 * 
	 * @param fileId ファイルID
	 * @return 申請ファイル一覧
	 */
	@Query(value = "SELECT file_id, application_id, application_file_id, upload_file_name, file_path FROM o_application_file WHERE file_id = :fileId ORDER BY file_id ASC", nativeQuery = true)
	List<ApplicationFile> getApplicationFile(@Param("fileId") Integer fileId);

}
