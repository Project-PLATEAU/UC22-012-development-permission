import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import Button, { RawButton } from "../../../../Styled/Button";
import CustomStyle from "./scss/upload-applicant-Information.scss";
import Config from "../../../../../customconfig.json";
/**
 * 申請ファイル登録画面
 */
@observer
class UploadApplicationInformation extends React.Component {
    static displayName = "UploadApplicationInformation";

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
            //申請ファイル一覧
            applicationFile: props.viewState.applicationFile,
        };
    }

    componentDidMount() {
        //保持された入力情報がある場合はAPIリクエストを行わない
        if (Object.keys(this.state.applicationFile).length < 1) {
            // APIへのリクエスト
            let generalConditionDiagnosisResult = Object.values(this.props.viewState.generalConditionDiagnosisResult);
            generalConditionDiagnosisResult = generalConditionDiagnosisResult.filter(Boolean);
            fetch(Config.config.apiUrl + "/application/applicationFiles", {
                method: 'POST',
                body: JSON.stringify(generalConditionDiagnosisResult),
                headers: new Headers({ 'Content-type': 'application/json' }),
            })
                .then(res => res.json())
                .then(res => {
                    if (Object.keys(res).length > 0 && !res.status) {
                        Object.keys(res).map(key => {
                            if (!res[key].uploadFileFormList) {
                                res[key].uploadFileFormList = [];
                            }
                        })
                        this.setState({ applicationFile: Object.values(res) });
                    } else if (res.status) {
                        this.setState({ applicationFile: [] });
                        alert("申請ファイル一覧の取得に失敗しました。");
                    } else {
                        this.setState({ applicationFile: [] });
                        alert("該当する申請ファイルは一件もありません。");
                    }
                }).catch(error => {
                    this.setState({ applicationFile: [] });
                    console.error('処理に失敗しました', error);
                    alert('処理に失敗しました');
                });
        }
        this.draggable(document.getElementById('UploadApplicationInformationDrag'), document.getElementById('UploadApplicationInformation'));
    }

    // 次へ
    next() {
        let permission = true;
        let applicationFile = this.state.applicationFile;
        Object.keys(applicationFile).map(key => {
            if (applicationFile[key].requireFlag) {
                if ((applicationFile[key].applicationFileId !== 9999 && applicationFile[key].applicationFileId !== '9999') && (!applicationFile[key] || Object.keys(applicationFile[key]["uploadFileFormList"])?.length < 1)) {
                    permission = false;
                }
            }
        });
        if (!permission) {
            alert("登録されていない必須ファイルがあります");
        } else {
            // 申請内容確認へ遷移
            this.props.viewState.nextConfirmApplicationDetailsView(this.state.applicationFile);
        }
    }

    // 戻る
    back() {
        //　入力情報を保持して申請者情報に戻る
        this.props.viewState.backEnterApplicantInformationView(this.state.applicationFile);
    }

    /**
     * アップロード処理
     * @param {event} event
     * @param {number} 対象申請ファイルのindex
     */
    fileUpload(event, index) {
        const applicationFile = this.state.applicationFile;
        let files = event.target.files;
        if (files[0].size > 10485760) {
            alert("10M以下のファイルをアップロードできます");
            return false;
        };
        let reader = new FileReader();
        reader.readAsDataURL(files[0]);
        reader.onload = (e) => {
            let applicationFileId = applicationFile[index].applicationFileId;
            applicationFile[index]["uploadFileFormList"] = applicationFile[index]["uploadFileFormList"].filter((uploadApplicationFile) => uploadApplicationFile.uploadFileName !== files[0].name);
            applicationFile[index]["uploadFileFormList"].push({ "fileId": "", "applicantId": "", "applicationFileId": "", "uploadFileName": files[0].name, "filePath": files[0].name, "uploadFile": files[0] });
            this.setState({
                applicationFile: applicationFile,
            });
            document.getElementById("upload" + applicationFileId).value = "";
        }
    }

    /**
     * ファイル削除
     * @param {event} event
     * @param {number} 対象申請ファイルのindex
     */
    fileDelete(event, index) {
        let applicationFile = this.state.applicationFile;
        applicationFile[index]["uploadFileFormList"] = [];
        this.setState({
            applicationFile: applicationFile,
        });
    }

    /**
     * コンポーネントドラッグ操作
     * @param {Object} ドラッグ操作対象
     * @param {Object} ドラッグ対象
     */
    draggable(target, content) {
        target.onmousedown = function () {
            document.onmousemove = mouseMove;
        };
        document.onmouseup = function () {
            document.onmousemove = null;
        };
        function mouseMove(e) {
            var event = e ? e : window.event;
            content.style.top = (event.clientY + (parseInt(content.clientHeight) / 2) - 10) + 'px';
            content.style.left = event.clientX + 'px';
        }
    }

    render() {
        const title = "2. 申請ファイル";
        const explanation = "下記ファイルの登録をお願いします（1ファイルあたり上限10Mまで）。";
        const applicationFile = this.state.applicationFile;
        return (
            <Box
                displayInlineBlock
                backgroundColor={this.props.theme.textLight}
                styledWidth={"600px"}
                styledHeight={"600px"}
                fullHeight
                id="UploadApplicationInformation"
                css={`
          position: fixed;
          z-index: 9992;
        `}
                className={CustomStyle.custom_frame}
            >
                <Box position="absolute" paddedRatio={3} topRight>
                    <RawButton onClick={() => {
                        this.props.viewState.hideUploadApplicationInformationView();
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
                <nav className={CustomStyle.custom_nuv} id="UploadApplicationInformationDrag">
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
                    <div className={CustomStyle.scrollContainer}>
                        {Object.keys(applicationFile).length > 0 && (
                            <table className={CustomStyle.selection_table}>
                                <thead>
                                    <tr className={CustomStyle.table_header}>
                                        <th style={{ width: 185 + "px" }}>対象</th>
                                        <th style={{ width: 50 + "px" }}>拡張子</th>
                                        <th style={{ width: 160 + "px" }}>ファイル名</th>
                                        <th style={{ width: 120 + "px" }}></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {Object.keys(applicationFile).map(key => (
                                        (applicationFile[key].applicationFileId !== 9999 && applicationFile[key].applicationFileId !== '9999' && (
                                        <tr key={applicationFile[key].applicationFileId}>
                                            <td>
                                                {!applicationFile[key].requireFlag && (
                                                    "("
                                                )}
                                                {applicationFile[key].applicationFileName}
                                                {!applicationFile[key].requireFlag && (
                                                    ")"
                                                )}
                                            </td>
                                            <td>pdf</td>
                                            <td>{applicationFile[key] && (
                                                applicationFile[key]["uploadFileFormList"]?.map(uploadApplicationFile => { return uploadApplicationFile.uploadFileName }).filter(uploadFileName => { return uploadFileName !== null }).join(",")
                                            )}</td>
                                            <td>
                                                <div className={CustomStyle.table_button_box}>
                                                    <div className={CustomStyle.item}>
                                                        {Object.keys(applicationFile[key]["uploadFileFormList"]).length >= 1 && (
                                                            <label className={CustomStyle.add_button_label} tabIndex="0">
                                                                <input type="file" className={CustomStyle.upload_button} onChange={e => { this.fileUpload(e, key) }} accept=".pdf" id={"upload" + applicationFile[key].applicationFileId} />
                                                                追加
                                                            </label>
                                                        )}
                                                        {Object.keys(applicationFile[key]["uploadFileFormList"]).length < 1 && (
                                                            <label className={CustomStyle.upload_button_label} tabIndex="0">
                                                                <input type="file" className={CustomStyle.upload_button} onChange={e => { this.fileUpload(e, key) }} accept=".pdf" id={"upload" + applicationFile[key].applicationFileId} />
                                                                登録
                                                            </label>
                                                        )}
                                                    </div>
                                                    <div className={CustomStyle.item}>
                                                        <button
                                                            className={CustomStyle.upload_delete_button}
                                                            onClick={e => {
                                                                this.fileDelete(e, key)
                                                            }}
                                                        >
                                                            <span>削除</span>
                                                        </button>
                                                    </div>
                                                </div></td>
                                        </tr>
                                        ))
                                    ))}
                                </tbody>
                            </table>
                        )}
                    </div>
                    <Spacing bottom={3} />
                    <div className={CustomStyle.box}>
                        <div className={CustomStyle.item} style={{ width: 48 + "%" }}>
                            <button
                                className={CustomStyle.next_button}
                                onClick={e => {
                                    if (Object.keys(applicationFile).length > 0) {
                                        this.next();
                                    }
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
                </Box>
            </Box >
        );
    }
}
export default withTranslation()(withTheme(UploadApplicationInformation));