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
import Button, { RawButton } from "../../../../Styled/Button";
import CustomStyle from "./scss/enter-applicant-Information.scss";
import Config from "../../../../../customconfig.json";
/**
 * 申請者情報入力画面
 */
@observer
class EnterApplicantInformation extends React.Component {
    static displayName = "EnterApplicantInformation";

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
            //申請者情報一覧
            applicantInformation: props.viewState.applicantInformation,
            //申請者情報入力エラー一覧
            errorItems: {}
        };
    }

    componentDidMount() {
        //保持された入力情報がある場合はAPIリクエストを行わない
        if (this.state.applicantInformation.length < 1) {
            // APIへのリクエスト
            fetch(Config.config.apiUrl + "/application/applicantItems/")
            .then(res => res.json())
            .then(res => {
                if (Object.keys(res).length > 0 && !res.status) {
                    this.setState({applicantInformation:res});
                }else{
                    alert('申請者情報入力項目一覧取得に失敗しました');
                }
            }).catch(error => {
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
            });
        }
        this.draggable(document.getElementById('EnterApplicantInformationDrag'),document.getElementById('EnterApplicantInformation'));
    }

    /**
     * 入力値をセット
     * @param {number} 対象のindex
     * @param {string} 入力された値
     */
    inputChange(index, value) {
        let applicantInformation = this.state.applicantInformation;
        applicantInformation[index].value = value;
        this.setState({ applicantInformation: applicantInformation });
    }

    //次へ
    next() {
        const applicantInformation = this.state.applicantInformation;
        let errorItems = this.state.errorItems;
        let reg = "";
        // 必須入力チェック & 個別の形式チェック
        Object.keys(applicantInformation).map(key => {
            if (applicantInformation[key].requireFlag) {
                if (!applicantInformation[key].value) {
                    errorItems[applicantInformation[key].id] = "必須項目です";
                } else {
                    delete errorItems[applicantInformation[key].id];
                    if (applicantInformation[key].regularExpressions) {
                        reg = new RegExp(applicantInformation[key].regularExpressions);
                        if (!reg.exec(applicantInformation[key].value)) {
                            errorItems[applicantInformation[key].id] = applicantInformation[key].name + "の形式を正しく入力してください";
                        } else {
                            delete errorItems[applicantInformation[key].id];
                        }
                    }
                }
            }
        })
        if (Object.keys(errorItems).length > 0) {
            this.setState({ errorItems: errorItems });
        } else {
            // ファイルアップロードへ遷移
            this.props.viewState.nextUploadApplicationInformationView(Object.assign({}, this.state.applicantInformation));
        }
    }

    //戻る
    back() {
        this.props.viewState.backGeneralConditionDiagnosisView();
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
        const title = "1. 申請者情報";
        const explanation = "下記に入力してください";
        const applicantInformation = this.state.applicantInformation;
        const errorItems = this.state.errorItems;
        return (
            <Box
                displayInlineBlock
                backgroundColor={this.props.theme.textLight}
                styledWidth={"500px"}
                styledHeight={"600px"}
                fullHeight
                id="EnterApplicantInformation"
                css={`
          position: fixed;
          z-index: 9992;
        `}
                className={CustomStyle.custom_frame}
            >
                <Box position="absolute" paddedRatio={3} topRight>
                    <RawButton onClick={() => {
                        this.props.viewState.hideEnterApplicantInformationView();
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
                <nav className={CustomStyle.custom_nuv} id="EnterApplicantInformationDrag">
                    申請フォーム
                </nav>
                <Box
                    centered
                    paddedHorizontally={5}
                    paddedVertically={10}
                    displayInlineBlock
                    className={CustomStyle.custom_content}
                >
                    <h2 className={CustomStyle.title}>{title}</h2>
                    <p className={CustomStyle.explanation}>{explanation}</p>
                    <Spacing bottom={1} />
                    <div className={CustomStyle.scrollContainer}>
                    {Object.keys(applicantInformation).map(key => (
                        <div className={CustomStyle.box}>
                            <div className={CustomStyle.caption}>
                                <Text>
                                    {!applicantInformation[key].requireFlag && (
                                        "("
                                    )}
                                    {applicantInformation[key].name}
                                    {!applicantInformation[key].requireFlag && (
                                        ")"
                                    )}</Text>
                            </div>
                            <div className={CustomStyle.input_text}>
                                {errorItems[applicantInformation[key].id] && (
                                    <p className={CustomStyle.error}>{errorItems[applicantInformation[key].id]}</p>
                                )}
                                <Input
                                    light={true}
                                    dark={false}
                                    type="text"
                                    value={applicantInformation[key].value}
                                    placeholder={applicantInformation[key].name + "を入力してください"}
                                    id={applicantInformation[key].id}
                                    onChange={e => this.inputChange(key, e.target.value)}
                                />
                            </div>

                        </div>
                    ))}
                    </div>
                    <Spacing bottom={3} />
                    {Object.keys(applicantInformation).length > 0 && (
                        <div className={CustomStyle.box}>
                            <div className={CustomStyle.item} style={{ width: 48 + "%" }}>
                                <button
                                    className={CustomStyle.next_button}
                                    onClick={e => {
                                        this.next();
                                    }}
                                >
                                    <span>次へ</span>
                                </button>
                            </div>
                            <div className={CustomStyle.item} style={{ width: 48 + "%" }}>
                                <button
                                    className={CustomStyle.back_button}
                                    onClick={e => {
                                        this.back();
                                    }}
                                >
                                    <span>戻る</span>
                                </button>
                            </div>
                        </div>
                    )}
                </Box>
            </Box >
        );
    }
}
export default withTranslation()(withTheme(EnterApplicantInformation));