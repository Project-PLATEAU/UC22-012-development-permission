import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Spacing from "../../../../Styled/Spacing";
import Text from "../../../../Styled/Text";
import Input from "../../../../Styled/Input";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/answer-login.scss";
import Config from "../../../../../customconfig.json";
import Button, { RawButton } from "../../../../Styled/Button";
/**
 * 回答確認ログイン画面
 */
@observer
class AnswerLogin extends React.Component {
    static displayName = "AnswerLogin";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            //ID
            id: "",
            //パスワード
            password: "",
            //入力エラー
            errorItems: {}
        };
    }

    componentDidMount() {
        this.draggable(document.getElementById('AnswerLoginDrag'),document.getElementById('AnswerLogin'));
    }

    //ログイン処理
    confirm(){
        const id = this.state.id;
        const password = this.state.password;
        let errorItems = this.state.errorItems;
        // 必須項目のチェック
        if(!id){
            errorItems["id"] = "必須項目です";
        }else{
            delete errorItems["id"];
        }
        if(!password){
            errorItems["password"] = "必須項目です";
        }else{
            delete errorItems["password"];
        }
        // エラーがある場合は処理を中断　エラーが無ければAPIへログイン情報を送信しログイン可否を決定する
        if (Object.keys(errorItems).length > 0) {
            this.setState({ errorItems: errorItems });
        } else {
            fetch(Config.config.apiUrl + "/answer/confirm/answer", {
                method: 'POST',
                body: JSON.stringify({
                    loginId:id,
                    password:password,
                }),
                headers: new Headers({ 'Content-type': 'application/json' }),
            })
            .then(res => res.json())
            .then(res => {
                if (res.applicationId) {
                    res.loginId = id;
                    res.password = password;
                    this.state.viewState.showAnswerContentView(JSON.parse(JSON.stringify(res)));
                }else{
                    alert('ログイン処理に失敗しました');
                }
            }).catch(error => {
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
            });
        }
    }

    /**
     * コンポーネントドラッグ操作
     * @param {Object} ドラッグ操作対象
     * @param {Object} ドラッグ対象
     */
    draggable(target,content) {
        target.onmousedown = function() {
            document.onmousemove = mouseMove;
        };
        document.onmouseup = function() {
            document.onmousemove = null;
        };
        function mouseMove(e) {
            var event = e ? e : window.event;
            content.style.top = (event.clientY + (parseInt(content.clientHeight)/2) - 10) + 'px';
            content.style.left = event.clientX + 'px';
        }
    }
    
    render() {
        const explanation = "ID/パスワードを入力してください";
        const id = this.state.id;
        const password = this.state.password;
        const errorItems = this.state.errorItems;
        return (
            <Box
                displayInlineBlock
                backgroundColor={this.props.theme.textLight}
                styledWidth={"500px"}
                styledHeight={"300px"}
                fullHeight
                id="AnswerLogin"
                overflow={"auto"}
                css={`
          position: fixed;
          z-index: 9992;
        `}
                className={CustomStyle.custom_frame}
            >
                <Box position="absolute" paddedRatio={3} topRight>
                    <RawButton onClick={() => {
                        this.props.viewState.hideAnswerLoginView();
                    }}>
                        <StyledIcon
                            styledWidth={"16px"}
                            fillColor={this.props.theme.textLight}
                            opacity={"0.5"}
                            glyph={Icon.GLYPHS.closeLight}
                            css={`
                            cursor:pointer;
                          `}
                        />
                    </RawButton>
                </Box>
                <nav className={CustomStyle.custom_nuv} id="AnswerLoginDrag">
                    申請・回答内容確認
                </nav>
                <Box
                    centered
                    paddedHorizontally={5}
                    paddedVertically={10}
                    displayInlineBlock
                    className={CustomStyle.custom_content}
                >
                    <p className={CustomStyle.explanation}>{explanation}</p>
                    <Spacing bottom={1} />
                    <div className={CustomStyle.box}>
                        <div className={CustomStyle.caption}>
                            <Text>ID</Text>
                        </div>
                        <div className={CustomStyle.input_text}>
                            {errorItems["id"] && (
                                <p className={CustomStyle.error}>{errorItems["id"]}</p>
                            )}
                            <Input
                                light={true}
                                dark={false}
                                type="text"
                                value={id}
                                placeholder=""
                                id="id"
                                onChange={e => this.setState({ id: e.target.value })}
                            />
                        </div>
                    </div>
                    <div className={CustomStyle.box}>
                        <div className={CustomStyle.caption}>
                            <Text>パスワード</Text>
                        </div>
                        <div className={CustomStyle.input_text}>
                            {errorItems["password"] && (
                                <p className={CustomStyle.error}>{errorItems["password"]}</p>
                            )}
                            <Input
                                light={true}
                                dark={false}
                                type="text"
                                value={password}
                                placeholder=""
                                id="password"
                                onChange={e => this.setState({ password: e.target.value })}
                            />
                        </div>
                    </div>
                    <Spacing bottom={3} />
                    <div className={CustomStyle.box}>
                        <div className={CustomStyle.item} style={{ width: 48 + "%" }}>
                            <button
                                className={CustomStyle.next_button}
                                onClick={e => {
                                    this.confirm();
                                }}
                            >
                                <span>確認</span>
                            </button>
                        </div>
                        <div className={CustomStyle.item} style={{ width: 48 + "%" }}>
                            <button
                                className={CustomStyle.back_button}
                                onClick={e => {
                                    this.props.viewState.hideAnswerLoginView();
                                }}
                            >
                                <span>戻る</span>
                            </button>
                        </div>
                    </div>
                </Box>
            </Box >
        );
    }
}
export default withTranslation()(withTheme(AnswerLogin));