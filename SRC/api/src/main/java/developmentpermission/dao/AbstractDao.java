package developmentpermission.dao;

import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;

/**
 * DAO共通処理(テーブル名のようなものをSQLで動的に変更したい場合はEntityが使用できないのでDAOを使用する)
 */
@Transactional
public abstract class AbstractDao {

	/** Entityマネージャファクトリ */
	protected EntityManagerFactory emf;

	/**
	 * コンストラクタ
	 * 
	 * @param emf Entityマネージャファクトリ
	 */
	protected AbstractDao(EntityManagerFactory emf) {
		this.emf = emf;
	}

	/**
	 * WHERE句の連結用文字列処理
	 * @param sb where句文字列
	 */
	protected void appendWhereText(StringBuffer sb) {
		if (sb.length() == 0) {
			sb.append("WHERE ");
		} else {
			sb.append("AND ");
		}
	}
}
