package developmentpermission.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.form.DistrictNameForm;
import developmentpermission.form.GetLotNuberByFigureForm;
import developmentpermission.form.GetLotNumberForm;
import developmentpermission.form.LotNumberForm;
import developmentpermission.form.LotNumberSearchConditionForm;
import developmentpermission.form.LotNumberSearchResultForm;
import developmentpermission.form.ResponseEntityForm;
import developmentpermission.service.LotNumberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 
 * 地番系API Controllerクラス.
 *
 */
@Api(tags = "地番")
@RestController
@RequestMapping("/lotnumber")
public class LotNumberApiController extends AbstractApiController {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(LotNumberApiController.class);

	/** 地番Serviceインスタンス */
	@Autowired
	private LotNumberService lotNumberService;

	/**
	 * 地番検索テーブル項目一覧取得
	 * 
	 * @return List<LotNumberSearchResultForm> 地番検索テーブル項目一覧
	 */
	@RequestMapping(value = "/columns", method = RequestMethod.GET)
	@ApiOperation(value = "地番検索テーブル項目一覧取得", notes = "地番検索テーブル項目一覧を取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public List<LotNumberSearchResultForm> getLotNumberColumns() {
		LOGGER.info("地番検索テーブル項目一覧取得 開始");
		try {
			List<LotNumberSearchResultForm> lotNumberSearchResultFormList = lotNumberService.getLotNumberColumns();
			return lotNumberSearchResultFormList;
		} finally {
			LOGGER.info("地番検索テーブル項目一覧取得 終了");
		}
	}

	/**
	 * 町丁目名一覧取得
	 * 
	 * @return List<DistrictNameFrom> 町丁目名一覧
	 */
	@RequestMapping(value = "/districts", method = RequestMethod.GET)
	@ApiOperation(value = "町丁目名一覧取得", notes = "町丁目名一覧を取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public List<DistrictNameForm> getDistricts() {
		LOGGER.info("町丁目名一覧取得 開始");
		try {
			List<DistrictNameForm> districtFormList = lotNumberService.getDistricts();
			return districtFormList;
		} finally {
			LOGGER.info("町丁目名一覧取得 終了");
		}
	}

	/**
	 * 地番検索（事業者向け）
	 * 
	 * @param lotNumberSearchConditionForm 地番検索条件
	 * @return List<LotNumberForm> 地番検索結果一覧
	 */
	@RequestMapping(value = "/search/estabrishment", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "地番検索（事業者）", notes = "地番を検索する（事業者向け）.statusはnullで返却")
	@ResponseBody
	@ApiResponses(value = { 
			@ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public List<LotNumberForm> searchLotNumber(@ApiParam(required = true, value = "地番検索条件フォーム")@RequestBody LotNumberSearchConditionForm lotNumberSearchConditionForm) {
		LOGGER.info("地番検索（事業者） 開始");
		try {
			List<LotNumberForm> lotNumberSearchFormList = lotNumberService
					.searchLotNumber(lotNumberSearchConditionForm);
			return lotNumberSearchFormList;
		} finally {
			LOGGER.info("地番検索（事業者） 終了");
		}
	}

	/**
	 * 地番検索（行政向け）
	 * 
	 * @param lotNumberSearchConditionForm 地番検索条件
	 * @return List<LotNumberForm> 地番検索結果一覧
	 */
	@RequestMapping(value = "/search/goverment", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "地番検索（行政）", notes = "地番を検索する（行政向け）.statusも含めて返却.")
	@ResponseBody
	@ApiResponses(value = { 
			@ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public List<LotNumberForm> searchLotNumberByGoverment(
			@ApiParam(required = true, value = "地番検索条件フォーム")@RequestBody LotNumberSearchConditionForm lotNumberSearchConditionForm) {
		LOGGER.info("地番検索（行政） 開始");
		try {
			List<LotNumberForm> lotNumberSearchFormList = lotNumberService
					.searchLotNumberByGoverment(lotNumberSearchConditionForm);
			return lotNumberSearchFormList;
		} finally {
			LOGGER.info("地番検索（行政） 終了");
		}
	}

	/**
	 * 緯度経度から地番取得（事業者）
	 * 
	 * @param lotNumberSearchConditionForm 地番検索条件
	 * @return List<LotNumberForm> 地番検索結果一覧
	 */
	@RequestMapping(value = "/getFromLonlat/establishment", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "地番取得（事業者）", notes = "緯度経度から地番を取得する")
	@ResponseBody
	@ApiResponses(value = { 
			@ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public List<LotNumberForm> getLotNumberFromLonlat(@ApiParam(required = true, value = "地番取得フォーム")@RequestBody GetLotNumberForm getLotNumberForm) {
		LOGGER.info("座標地番検索（事業者） 開始");
		try {
			String longitude = getLotNumberForm.getLongitude();
			String latitude = getLotNumberForm.getLatiude();
			if (longitude != null && latitude != null) {
				List<LotNumberForm> lotNumberSearchFormList = lotNumberService.searchLotNumberFromLonlat(longitude,
						latitude);
				return lotNumberSearchFormList;
			} else {
				LOGGER.error("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("座標地番検索（事業者） 終了");
		}
	}

	/**
	 * 緯度経度から申請中の地番取得（行政）
	 * 
	 * @param lotNumberSearchConditionForm 地番検索条件
	 * @return List<LotNumberForm> 地番検索結果一覧
	 */
	@RequestMapping(value = "/getFromLonlat/goverment", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "申請中の地番取得（行政）", notes = "緯度経度から申請中の地番を取得する")
	@ResponseBody
	@ApiResponses(value = { 
			@ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public List<LotNumberForm> getApplyingLotNumberFromLonlat(@ApiParam(required = true, value = "地番取得フォーム")@RequestBody GetLotNumberForm getLotNumberForm) {
		LOGGER.info("座標地番検索（行政） 開始");
		try {
			String longitude = getLotNumberForm.getLongitude();
			String latitude = getLotNumberForm.getLatiude();
			if (longitude != null && latitude != null) {
				List<LotNumberForm> lotNumberSearchFormList = lotNumberService
						.searchApplyingLotNumberFromLonlat(longitude, latitude);
				return lotNumberSearchFormList;
			} else {
				LOGGER.error("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("座標地番検索（行政） 終了");
		}
	}

	/**
	 * 図形地番検索
	 * 
	 * @param getLotNuberByFigureForm 図形情報
	 * @return List<LotNumberForm> 地番検索結果一覧
	 */
	@RequestMapping(value = "/getFromFigure/establishment", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "図形による地番取得（事業者）", notes = "指定した図形範囲に含まれる地番を取得する")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 406, message = "上限件数以上の地番が取得された場合", response = ResponseEntityForm.class)})
	public List<LotNumberForm> getLotNumberFromLonlat(@ApiParam(required = true, value = "範囲選択地番取得フォーム")@RequestBody GetLotNuberByFigureForm getLotNuberByFigureForm) {
		LOGGER.info("図形地番検索（事業者） 開始");
		try {
			if (getLotNuberByFigureForm.getCoodinates() != null) {
				List<LotNumberForm> lotNumberSearchFormList = lotNumberService
						.searchLonLatByFigure(getLotNuberByFigureForm.getCoodinates());
				return lotNumberSearchFormList;
			} else {
				LOGGER.error("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("図形地番検索（事業者） 終了");
		}
	}
}
