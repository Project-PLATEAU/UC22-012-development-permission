package developmentpermission.service;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.Application;
import developmentpermission.controller.LotNumberApiController;
import developmentpermission.dao.LotNumberDao;
import developmentpermission.entity.District;
import developmentpermission.entity.LotNumberAndDistrict;
import developmentpermission.entity.LotNumberSearchResultDefinition;
import developmentpermission.form.DistrictNameForm;
import developmentpermission.form.LotNumberForm;
import developmentpermission.form.LotNumberSearchConditionForm;
import developmentpermission.form.LotNumberSearchResultForm;
import developmentpermission.repository.DistrictRepository;
import developmentpermission.repository.LotNumberSearchResultDefinitionRepository;

/**
 * 地番Serviceクラス
 */
@Service
@Transactional
public class LotNumberService extends AbstractService {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(LotNumberService.class);

	/** M_地番検索結果定義Repositoryインスタンス */
	@Autowired
	private LotNumberSearchResultDefinitionRepository lotNumberSearchResultDefinitionRepository;

	/** F_大字Repositoryインスタンス */
	@Autowired
	private DistrictRepository districtRepository;

	/**
	 * 地番検索テーブル項目一覧取得
	 * 
	 * @return 地番検索テーブル項目一覧
	 */
	public List<LotNumberSearchResultForm> getLotNumberColumns() {
		List<LotNumberSearchResultForm> formList = new ArrayList<LotNumberSearchResultForm>();

		List<LotNumberSearchResultDefinition> lotNumberSearchResultDefinitionList = lotNumberSearchResultDefinitionRepository
				.getLotNumberSearchResultDefinitionList();
		for (LotNumberSearchResultDefinition lotNumberSearchResultDefinition : lotNumberSearchResultDefinitionList) {
			formList.add(getLotNumberSearchResultFormFromEntity(lotNumberSearchResultDefinition));
		}
		return formList;
	}

	/**
	 * 町丁目名一覧取得
	 * 
	 * @return 町丁目名一覧
	 */
	public List<DistrictNameForm> getDistricts() {
		List<DistrictNameForm> formList = new ArrayList<DistrictNameForm>();

		List<District> districtList = districtRepository.getDistinctList();
		for (District district : districtList) {
			formList.add(getDistrictNameFormFromEntity(district));
		}
		return formList;
	}

	/**
	 * 地番検索（事業者）
	 * 
	 * @param lotNumberSearchConditionForm 検索条件
	 * @return 地番一覧
	 */
	public List<LotNumberForm> searchLotNumber(LotNumberSearchConditionForm lotNumberSearchConditionForm) {
		return searchLotNumber(lotNumberSearchConditionForm, false);
	}

	/**
	 * 地番検索（行政）
	 * 
	 * @param lotNumberSearchConditionForm 検索条件
	 * @return 地番一覧
	 */
	public List<LotNumberForm> searchLotNumberByGoverment(LotNumberSearchConditionForm lotNumberSearchConditionForm) {
		return searchLotNumber(lotNumberSearchConditionForm, true);
	}

	/**
	 * 緯度経度から申請中の地番取得（行政）
	 * 
	 * @param longitude 経度
	 * @param latitude  緯度
	 * @return 地番一覧
	 */
	public List<LotNumberForm> searchApplyingLotNumberFromLonlat(String longitude, String latitude) {
		return searchLotNumberFromLonlat(longitude, latitude, true);
	}

	/**
	 * 緯度経度からの地番検索(事業者)
	 * 
	 * @param longitude 経度
	 * @param latitude  緯度
	 * @return 地番一覧
	 */
	public List<LotNumberForm> searchLotNumberFromLonlat(String longitude, String latitude) {
		return searchLotNumberFromLonlat(longitude, latitude, false);
	}

	/**
	 * 図形地番検索
	 * 
	 * @param coordinates
	 * @return
	 */
	public List<LotNumberForm> searchLonLatByFigure(double[][] coordinates) {
		String wkt = null;
		List<LotNumberForm> formList = new ArrayList<LotNumberForm>();
		try {
			wkt = getPolygonWktFromCoodinates(coordinates);
		} catch (Exception e) {
			LOGGER.error("パラメータの座標不正", e);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
		List<LotNumberSearchResultDefinition> lotNumberSearchResultDefinitionList = lotNumberSearchResultDefinitionRepository
				.getLotNumberSearchResultDefinitionList();
		LotNumberDao dao = new LotNumberDao(emf);
		final List<LotNumberAndDistrict> lotNumberList = dao.searchLotNumberLByFigure(wkt, epsg, lonlatEpsg,
				figureLotNumberLimit + 1);
		if (lotNumberList.size() > figureLotNumberLimit) {
			LOGGER.error("地番取得上限超過");
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
		}
		for (LotNumberAndDistrict lotNumber : lotNumberList) {
			formList.add(getLotNumberFormFromEntity(lotNumber, lotNumberSearchResultDefinitionList));
		}
		return formList;
	}

	/**
	 * 地番検索
	 * 
	 * @param lotNumberSearchConditionForm 検索条件
	 * @param isGoverment                  行政かどうか
	 * @return 地番一覧
	 */
	private List<LotNumberForm> searchLotNumber(LotNumberSearchConditionForm lotNumberSearchConditionForm,
			boolean isGoverment) {
		List<LotNumberForm> formList = new ArrayList<LotNumberForm>();

		List<LotNumberSearchResultDefinition> lotNumberSearchResultDefinitionList = lotNumberSearchResultDefinitionRepository
				.getLotNumberSearchResultDefinitionList();

		LotNumberDao dao = new LotNumberDao(emf);
		List<LotNumberAndDistrict> lotNumberList = dao.searchLotNumberList(lotNumberSearchConditionForm.getDistrictId(),
				lotNumberSearchConditionForm.getChiban(), lonlatEpsg, isGoverment);
		for (LotNumberAndDistrict lotNumber : lotNumberList) {
			formList.add(getLotNumberFormFromEntity(lotNumber, lotNumberSearchResultDefinitionList));
		}
		return formList;
	}

	/**
	 * 地番検索
	 * 
	 * @param longitude   経度
	 * @param latitude    緯度
	 * @param isGoverment 行政かどうか
	 * @return 地番一覧
	 */
	private List<LotNumberForm> searchLotNumberFromLonlat(String longitude, String latitude, boolean isGoverment) {
		List<LotNumberForm> formList = new ArrayList<LotNumberForm>();

		List<LotNumberSearchResultDefinition> lotNumberSearchResultDefinitionList = lotNumberSearchResultDefinitionRepository
				.getLotNumberSearchResultDefinitionList();

		LotNumberDao dao = new LotNumberDao(emf);
		List<LotNumberAndDistrict> lotNumberList = dao.searchLotNumberList(longitude, latitude, epsg, lonlatEpsg,
				isGoverment);
		for (LotNumberAndDistrict lotNumber : lotNumberList) {
			formList.add(getLotNumberFormFromEntity(lotNumber, lotNumberSearchResultDefinitionList));
		}
		return formList;
	}

	/**
	 * M_申請情報検索結果EntityをM_地番検索定義フォームに詰めなおす
	 * 
	 * @param entity M_申請情報検索結果Entity
	 * @return M_地番検索定義フォーム
	 */
	private LotNumberSearchResultForm getLotNumberSearchResultFormFromEntity(LotNumberSearchResultDefinition entity) {
		LotNumberSearchResultForm form = new LotNumberSearchResultForm();
		form.setLotNumberSearchDefinitionId(entity.getLotNumberSearchDefinitionId());
		form.setDisplayOrder(entity.getDisplayOrder());
		form.setTableType(entity.getTableType());
		form.setDisplayColumnName(entity.getDisplayColumnName());
		form.setTableWidth(entity.getTableWidth());
		form.setResponseKey(entity.getResponseKey());
		return form;
	}

	/**
	 * F_大字Entityを町丁目名フォームに詰めなおす
	 * 
	 * @param entity F_大字Entity
	 * @return 町丁目名フォーム
	 */
	private DistrictNameForm getDistrictNameFormFromEntity(District entity) {
		DistrictNameForm form = new DistrictNameForm();
		form.setId(entity.getDistrictId());
		form.setName(entity.getDistrictName());
		form.setKana(entity.getDistrictKana());
		return form;
	}

	/**
	 * 座標からポリゴンのwktを生成する.
	 * 
	 * @param coorinates
	 * @return
	 * @throws Exception
	 */
	private String getPolygonWktFromCoodinates(double[][] coordinates) throws Exception {
		try {
			StringBuilder sb = new StringBuilder();
			if (coordinates.length < 2) {
				throw new Exception("座標数不足");
			}
			if (coordinates[0][0] != coordinates[coordinates.length - 1][0]
					|| coordinates[0][0] != coordinates[coordinates.length - 1][0]) {
				throw new Exception("座標末尾不正");
			}
			sb.append("POLYGON((");
			for (int i = 0; i < coordinates.length; i++) {
				sb.append(coordinates[i][0]);
				sb.append(" ");
				sb.append(coordinates[i][1]);
				if (i < coordinates.length - 1) {
					sb.append(",");
				}
			}
			sb.append("))");
			return sb.toString();
		} catch (Exception e) {
			throw e;
		}
	}
}
