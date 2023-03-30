package developmentpermission.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import developmentpermission.service.AuthenticationService;

/**
 * Controller共通処理
 */
@RestController
public abstract class AbstractApiController {

	/** 認証系サービスインスタンス */
	@Autowired
	protected AuthenticationService authenticationService;
}
