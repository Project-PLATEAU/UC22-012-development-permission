package developmentpermission.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.form.ResponseEntityForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * ファイルAPIコントローラ
 */
@Api(tags = "ファイル")
@RestController
@RequestMapping("/file")
public class FileApiController extends AbstractApiController {
	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(FileApiController.class);

	@Value("${app.file.service.rootpath}")
	private String serviceFileBasePath;

	/**
	 * ファイルダウンロード
	 * 
	 * @return 申請者情報入力項目一覧
	 */
	@RequestMapping(value = "/view/**", method = RequestMethod.GET)
	@ApiOperation(value = "ファイル取得", notes = "ファイルを取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 404, message = "ファイルが存在しない場合", response = ResponseEntityForm.class),
			@ApiResponse(code = 500, message = "ファイルの取得に失敗した場合", response = ResponseEntityForm.class), })
	public HttpEntity<byte[]> getApplicantItems(HttpServletRequest request) {
		LOGGER.info("ファイル取得 開始");
		try {
			// ファイルパスを取得
			String uri = request.getRequestURI();
			final String fileName = (uri.contains("developmentpermissionapi"))
					? uri.replace("/developmentpermissionapi/file/view/", "")
					: uri.replace("/file/view/", "");
			LOGGER.info("取得するファイル名:" + fileName);
			// 絶対ファイルパス
			String absoluteFilePath = serviceFileBasePath + "/" + fileName;
			Path filePath = Paths.get(absoluteFilePath);

			if (!Files.exists(filePath)) {
				// ファイルが存在しない
				LOGGER.warn("ファイルが存在しない");
				throw new ResponseStatusException(HttpStatus.NOT_FOUND);
			}
			// リソースファイルを読み込み
			File file = new File(absoluteFilePath);
			InputStream is = new FileInputStream(file);
			// byteへ変換
			byte[] data = IOUtils.toByteArray(is);

			String mimeType = URLConnection.guessContentTypeFromStream(is);
			if (mimeType == null) {
				int point = fileName.lastIndexOf(".");
				String sp = fileName.substring(point + 1);
				if ("pdf".equals(fileName.substring(point + 1)) || "PDF".equals(fileName.substring(point + 1))) {
					mimeType = "application/pdf";
				} else if ("jpg".equals(fileName.substring(point + 1)) || "jpeg".equals(fileName.substring(point + 1))
						|| "JPG".equals(fileName.substring(point + 1))
						|| "JPEG".equals(fileName.substring(point + 1))) {
					mimeType = "image/jpeg";
				} else if ("png".equals(fileName.substring(point + 1))
						|| "PNG".equals(fileName.substring(point + 1))) {
					mimeType = "image/png";
				} else if ("tif".equals(fileName.substring(point + 1)) || "tiff".equals(fileName.substring(point + 1))
						|| "TIF".equals(fileName.substring(point + 1))
						|| "TIFF".equals(fileName.substring(point + 1))) {
					mimeType = "image/tiff";
				} else {
					mimeType = "application/octet-stream";
				}
			}
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Type", mimeType);
			headers.setContentLength(data.length);
			LOGGER.info("ファイル取得 終了");
			return new HttpEntity<byte[]>(data, headers);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
