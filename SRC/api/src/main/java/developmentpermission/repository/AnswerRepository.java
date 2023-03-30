package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.Answer;

/**
 * O_回答Repositoryインタフェース
 */
@Transactional
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Integer> {

	/**
	 * O_回答検索
	 * 
	 * @param answerId 回答ID
	 * @return 回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag FROM o_answer WHERE answer_id = :answerId", nativeQuery = true)
	List<Answer> findByAnswerId(@Param("answerId") Integer answerId);

	/**
	 * O_回答検索
	 * 
	 * @param applicationId 申請ID
	 * @return 回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag FROM o_answer WHERE application_id = :applicationId", nativeQuery = true)
	List<Answer> findByApplicationId(@Param("applicationId") Integer applicationId);

	/**
	 * 未回答検索
	 * 
	 * @param applicationId 申請ID
	 * @return 未回答一覧
	 */
	@Query(value = "SELECT answer_id, application_id, judgement_id, judgement_result, answer_content, notified_text, register_datetime, update_datetime, complete_flag, notified_flag FROM o_answer WHERE application_id = :applicationId AND complete_flag = '0'", nativeQuery = true)
	List<Answer> findUnansweredByApplicationId(@Param("applicationId") Integer applicationId);
}
