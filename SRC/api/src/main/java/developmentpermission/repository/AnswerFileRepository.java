package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.AnswerFile;

/**
 * O_回答ファイルRepositoryインタフェース
 */
@Transactional
@Repository
public interface AnswerFileRepository extends JpaRepository<AnswerFile, Integer> {

	/**
	 * O_回答ファイル検索
	 * 
	 * @param answerId 回答ID
	 * @return 回答ファイル一覧
	 */
	@Query(value = "SELECT answer_file_id, answer_id, answer_file_name, file_path, notified_file_path, delete_unnotified_flag FROM o_answer_file WHERE answer_id = :answerId ORDER BY answer_file_id ASC", nativeQuery = true)
	List<AnswerFile> findByAnswerId(@Param("answerId") Integer answerId);
	
	/**
	 * O_回答ファイル検索
	 * 
	 * @param answerId 回答ID
	 * @return 回答ファイル一覧
	 */
	@Query(value = "SELECT answer_file_id, answer_id, answer_file_name, file_path, notified_file_path, delete_unnotified_flag FROM o_answer_file WHERE answer_id = :answerId AND delete_unnotified_flag = '0' ORDER BY answer_file_id ASC", nativeQuery = true)
	List<AnswerFile> findByAnswerIdWithoutDeleted(@Param("answerId") Integer answerId);

	/**
	 * O_回答ファイル検索(事業者用)
	 * 
	 * @param answerId 回答ID
	 * @return 回答ファイル一覧
	 */
	@Query(value = "SELECT answer_file_id, answer_id, answer_file_name, file_path, notified_file_path, delete_unnotified_flag FROM o_answer_file WHERE answer_id = :answerId AND notified_file_path IS NOT NULL ORDER BY answer_file_id ASC", nativeQuery = true)
	List<AnswerFile> findByAnswerIdWithoutDeletedForBusiness(@Param("answerId") Integer answerId);
	
	/**
	 * O_回答ファイル検索
	 * 
	 * @param answerId 回答ID
	 * @return 回答ファイル一覧
	 */
	@Query(value = "SELECT answer_file_id, answer_id, answer_file_name, file_path, notified_file_path, delete_unnotified_flag FROM o_answer_file WHERE answer_file_id = :answerFileId ORDER BY answer_file_id ASC", nativeQuery = true)
	List<AnswerFile> findByAnswerFileId(@Param("answerFileId") Integer answerFileId);
}
