import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import Button, { RawButton } from "../../../../Styled/Button";
import CustomStyle from "./scss/answer-content.scss";
import Common from "../../../AdminDevelopCommon/CommonFixedValue.json";
import Config from "../../../../../customconfig.json";
/**
 * 申請・回答確認画面
 */
@observer
class AnswerContent extends React.Component {
    static displayName = "AnswerContent";
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
            //申請者情報
            applicantInformation: props.viewState.answerContent.applicantInformations,
            //申請区分
            checkedApplicationCategory: props.viewState.answerContent.applicationCategories,
            //申請ファイル
            applicationFile: props.viewState.answerContent.applicationFiles,
            //申請地番
            lotNumber: props.viewState.answerContent.lotNumbers,
            //申請状態
            status: props.viewState.answerContent.status,
            //行政からの回答一覧
            answers: props.viewState.answerContent.answers
        };
    }

    componentDidMount() {
        this.draggable(document.getElementById('AnswerContentDrag'),document.getElementById('AnswerContent'));
    }

    /**
     * ファイルダウンロード
     * @param {string} apiのpath
     * @param {object} 対象ファイル情報
     * @param {string} 対象ファイルの取得key
     */
    output(path, file, fileNameKey) {
        //ダウンロード時に認証が必要な為、申請ID及び照合IDとパスワードをセット
        if(file){
            file.applicationId = this.props.viewState.answerContent.applicationId;
            file.loginId = this.props.viewState.answerContent.loginId;
            file.password = this.props.viewState.answerContent.password;
        }
        // APIへのリクエスト
        fetch(Config.config.apiUrl + path, {
            method: 'POST',
            body: JSON.stringify(file),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
            .then((res) => res.blob())
            .then(blob => {
                const now = new Date();
                let anchor = document.createElement("a");
                anchor.href = window.URL.createObjectURL(blob);
                anchor.download = file[fileNameKey];
                anchor.click();
            })
            .catch(error => {
                console.error('処理に失敗しました', error);
                alert('処理に失敗しました');
            });
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
        const applicantInformation = this.state.applicantInformation;
        const checkedApplicationCategory = this.state.checkedApplicationCategory;
        const applicationFile = this.state.applicationFile;
        const status = this.state.status;
        const answers = this.state.answers;
        const lotNumber = this.state.lotNumber;
        let lotNumberResult = {};
        Object.keys(lotNumber).map(key => {
            if (!lotNumberResult[lotNumber[key].districtName]) {
                lotNumberResult[lotNumber[key].districtName] = new Array();
            }
            lotNumberResult[lotNumber[key].districtName].push(lotNumber[key].chiban);
        });
        return (
            <Box
                displayInlineBlock
                backgroundColor={this.props.theme.textLight}
                styledWidth={"600px"}
                styledHeight={"600px"}
                fullHeight
                id="AnswerContent"
                css={`
          position: fixed;
          z-index: 9992;
        `}
                className={CustomStyle.custom_frame}
            >
                <Box position="absolute" paddedRatio={3} topRight>
                    <RawButton onClick={() => {
                        this.props.viewState.hideAnswerContentView();
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
                <nav className={CustomStyle.custom_nuv} id="AnswerContentDrag">
                    回答内容確認
                </nav>
                <Box
                    centered
                    paddedHorizontally={5}
                    paddedVertically={10}
                    displayInlineBlock
                    className={CustomStyle.custom_content}
                >
                    <Spacing bottom={1} />
                    <button
                        className={CustomStyle.close_button}
                        onClick={e => {
                            this.props.viewState.hideAnswerContentView();
                        }}
                    >
                        <span>閉じる</span>
                    </button>
                    <Spacing bottom={1} />
                    <div className={CustomStyle.scrollContainer}>
                        <div className={CustomStyle.box}>
                            <div className={CustomStyle.item} style={{ width: 60 + "%" }}>
                                ■ステータス
                            </div>
                            <div className={CustomStyle.item} style={{ width: 40 + "%" }}>
                                {Common.status[status]}
                            </div>
                        </div>
                        <p>■申請者情報</p>
                        {applicantInformation && Object.keys(applicantInformation).map(key => (
                            <div className={CustomStyle.box}>
                                <div className={CustomStyle.item} style={{ width: 60 + "%" }}>
                                    ・{applicantInformation[key].name}
                                </div>
                                <div className={CustomStyle.item} style={{ width: 40 + "%" }}>
                                    {applicantInformation[key].value}
                                </div>
                            </div>
                        ))}
                        {checkedApplicationCategory && Object.keys(checkedApplicationCategory).map(key => (
                            <div className={CustomStyle.box}>
                                <div className={CustomStyle.item} style={{ width: 60 + "%" }}>
                                    ■{checkedApplicationCategory[key].title}
                                </div>
                                <div className={CustomStyle.item} style={{ width: 40 + "%" }}>
                                    {checkedApplicationCategory[key]["applicationCategory"]?.map(function (value) { return value.content }).filter(content => { return content !== null }).join(",")}
                                </div>
                            </div>
                        ))}
                        <div className={CustomStyle.box}>
                            <div className={CustomStyle.item} style={{ width: 60 + "%" }}>
                                ■申請地番
                            </div>
                            <div className={CustomStyle.item} style={{ width: 40 + "%" }}>
                                {lotNumberResult && Object.keys(lotNumberResult).map(key => (
                                    <p>{key} {lotNumberResult[key].map(chiban => { return chiban }).filter(chiban => { return chiban !== null }).join(",")}</p>
                                ))}
                            </div>
                        </div>
                        <Spacing bottom={3} />
                        <table className={CustomStyle.selection_table}>
                            <thead>
                                <tr className={CustomStyle.table_header}>
                                    <th style={{ width: 100 + "px" }}>申請ファイル</th>
                                    <th>対象</th>
                                    <th style={{ width: 50 + "px" }}>拡張子</th>
                                    <th style={{ width: 250 + "px" }}>ファイル名</th>
                                </tr>
                            </thead>
                            <tbody>
                                {applicationFile && Object.keys(applicationFile).map(index => (
                                    Object.keys(applicationFile[index]["uploadFileFormList"]).map(key => (
                                        applicationFile[index]["uploadFileFormList"][key].applicationFileId === "9999" && (
                                            <tr>
                                                <td>
                                                    <button
                                                        className={CustomStyle.download_button}
                                                        onClick={e => {
                                                            this.output("/application/file/download", applicationFile[index]["uploadFileFormList"][key], "uploadFileName");
                                                        }}
                                                    >
                                                        <span>ダウンロード</span>
                                                    </button>
                                                </td>
                                                <td>
                                                    {applicationFile[index].applicationFileName}
                                                </td>
                                                <td>xlsx</td>
                                                <td>
                                                    {applicationFile[index]["uploadFileFormList"][key].uploadFileName}
                                                </td>
                                            </tr>
                                        )
                                    ))
                                ))}
                                {applicationFile && Object.keys(applicationFile).map(index => (
                                    Object.keys(applicationFile[index]["uploadFileFormList"]).map(key => (
                                        applicationFile[index]["uploadFileFormList"][key].applicationFileId !== "9999" && (
                                            <tr>
                                                <td>
                                                    <button
                                                        className={CustomStyle.download_button}
                                                        onClick={e => {
                                                            this.output("/application/file/download", applicationFile[index]["uploadFileFormList"][key], "uploadFileName");
                                                        }}
                                                    >
                                                        <span>ダウンロード</span>
                                                    </button>
                                                </td>
                                                <td>
                                                    {applicationFile[index].applicationFileName}
                                                </td>
                                                <td>pdf</td>
                                                <td>
                                                    {applicationFile[index]["uploadFileFormList"][key].uploadFileName}
                                                </td>
                                            </tr>
                                        )
                                    ))
                                ))}
                            </tbody>
                        </table>
                        <Spacing bottom={3} />
                        <table className={CustomStyle.selection_table}>
                            <thead>
                                <tr className={CustomStyle.table_header}>
                                    <th style={{ width: 150 + "px" }}>対象</th>
                                    <th>判定結果</th>
                                    <th>回答内容</th>
                                </tr>
                            </thead>
                            <tbody>
                                {answers && Object.keys(answers).map(key => (
                                    <tr>
                                        <td>
                                            {answers[key]["judgementInformation"]["title"]}
                                        </td>
                                        <td>
                                            {answers[key]["judgementResult"]}
                                        </td>
                                        <td style={{ width: 200 + "px", overflow: "hidden", wordWrap: "break-word", whiteSpace: "pre-line" }}>
                                            {answers[key]["answerContent"]}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                        <Spacing bottom={3} />
                        <table className={CustomStyle.selection_table}>
                            <thead>
                                <tr className={CustomStyle.table_header}>
                                    <th style={{ width: 100 + "px" }}>回答ファイル</th>
                                    <th>対象</th>
                                    <th style={{ width: 50 + "px" }}>拡張子</th>
                                    <th>ファイル名</th>
                                </tr>
                            </thead>
                            <tbody>
                                {answers && Object.keys(answers).map(index => (
                                    Object.keys(answers[index]["answerFiles"]).map(key => (
                                        <tr>
                                            <td>
                                                <button
                                                    className={CustomStyle.download_button}
                                                    onClick={e => {
                                                        this.output("/answer/file/download", answers[index]["answerFiles"][key], "answerFileName");
                                                    }}
                                                >
                                                    <span>ダウンロード</span>
                                                </button>
                                            </td>
                                            <td>
                                                {answers[index]["judgementInformation"]["title"]}
                                            </td>
                                            <td style={{ width: 30 + "px" }}>pdf</td>
                                            <td style={{ width: 250 + "px", overflow: "hidden", wordWrap: "break-word" }}>
                                                {answers[index]["answerFiles"][key]["answerFileName"]}
                                            </td>
                                        </tr>
                                    ))
                                ))}
                            </tbody>
                        </table>
                    </div>
                </Box>
            </Box >
        );
    }
}
export default withTranslation()(withTheme(AnswerContent));