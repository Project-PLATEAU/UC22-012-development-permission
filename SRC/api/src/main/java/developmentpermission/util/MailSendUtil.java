package developmentpermission.util;

import org.aspectj.weaver.patterns.HasMemberTypePatternForPerThisMatching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;


/**
 * メール送信ユーティリティクラス.
 *
 *
 */
@Component
public class MailSendUtil {
	
	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(MailSendUtil.class);

	/** 送信元メールアドレス */
	private String sendFrom;

	/** メール送信を行うか否か */
	private boolean sendMailFlg;
	
	/** JavaMailSender */
	private JavaMailSenderImpl impl;

	/**
	 * コンストラクタ
	 * @param sendFrom 送信元メールアドレス
	 * @param sendHost 送信元ホスト
	 * @param sendPort 送信元ポート
	 * @param sendUserName 送信元ユーザ名
	 * @param sendPassword 送信元パスワード
	 * @param validSendMail メール送信フラグ
	 */
	@Autowired
	public MailSendUtil(@Value("${app.mail.sendfrom}") String sendFrom, @Value("${app.mail.host}") String sendHost,
			@Value("${app.mail.port}") String sendPort, @Value("${app.mail.username}") String sendUserName,
			@Value("${app.mail.password}") String sendPassword, @Value("${app.mail.validsendmail}") boolean validSendMail) {
		this.sendFrom = sendFrom;
		this.sendMailFlg = validSendMail;
		impl = new JavaMailSenderImpl();
		impl.setHost(sendHost);
		impl.setPort(Integer.parseInt(sendPort));
		impl.setUsername(sendUserName);
		impl.setPassword(sendPassword);
	}

	/**
	 * メールを送信
	 * 
	 * @param sendTo  宛先
	 * @param subject 件名
	 * @param content 本文
	 */
	public void sendMail(String sendTo, String subject, String content) throws Exception {
		if (sendMailFlg) {
			LOGGER.trace("メール送信 開始");
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setFrom(sendFrom);
			mailMessage.setTo(sendTo);
			mailMessage.setSubject(subject);
			mailMessage.setText(content);
			try {
				impl.send(mailMessage);
			} catch (MailException e) {
				LOGGER.error("メールの送信に失敗: ", e);
				throw e;
			}
			LOGGER.trace("メール送信 終了");
		} else {
			LOGGER.trace("メール送信 無効");
		}
	}
}
