import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import CustomStyle from "./scss/application-details.scss";
import Common from "../../../AdminDevelopCommon/CommonFixedValue.json";
import Config from "../../../../../customconfig.json";
/**
 * 申請情報詳細画面
 */
@observer
class ApplicationDetails extends React.Component {

    static displayName = "ApplicationDetails";

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
            applicantInformation: [],
            //申請区分
            checkedApplicationCategory: [],
            //申請ファイル
            applicationFile: [],
            //回答
            answers: [],
            //申請地番
            lotNumber: [],
            //申請状態
            status: "",
            //回答通知権限
            notificable: false,
            //申請ID
            applicationId: null,
            //申請回答DTO
            ApplyAnswerForm: {}
        };
    }

    //サーバからデータを取得
    componentDidMount() {
        if (this.props.viewState.applicationInformationSearchForApplicationId) {
            fetch(Config.config.apiUrl + "/application/detail/" + this.props.viewState.applicationInformationSearchForApplicationId)
                .then(res => res.json())
                .then(res => {
                    if (res.applicationId) {
                        this.setState({
                            applicantInformation: res.applicantInformations,
                            checkedApplicationCategory: res.applicationCategories,
                            applicationFile: res.applicationFiles,
                            answers: res.answers,
                            lotNumber: res.lotNumbers,
                            status: res.status,
                            notificable: res.notificable,
                            applicationId: res.applicationId,
                            ApplyAnswerForm: res
                        });
                    } else {
                        alert("申請情報詳細取得に失敗しました。再度操作をやり直してください。");
                    }
                }).catch(error => {
                    console.error('通信処理に失敗しました', error);
                    alert('通信処理に失敗しました');
                }).finally(() => document.getElementById("customloader_sub").style.display = "none");
        } else {
            alert("申請情報詳細取得に失敗しました。再度操作をやり直してください。");
            document.getElementById("customloader_sub").style.display = "none";
        }
        this.draggable(document.getElementById('ApplicationDetailsDrag'),document.getElementById('ApplicationDetails'));
    }

    //status更新用リフレッシュ
    refresh(){
        document.getElementById("customloader_sub").style.display = "block"
        fetch(Config.config.apiUrl + "/application/detail/" + this.props.viewState.applicationInformationSearchForApplicationId)
        .then(res => res.json())
        .then(res => {
            if (res.applicationId) {
                this.setState({
                    applicantInformation: res.applicantInformations,
                    checkedApplicationCategory: res.applicationCategories,
                    applicationFile: res.applicationFiles,
                    answers: res.answers,
                    lotNumber: res.lotNumbers,
                    status: res.status,
                    notificable: res.notificable,
                    applicationId: res.applicationId,
                    ApplyAnswerForm: res
                });
            } else {
                alert("申請情報詳細取得に失敗しました。再度操作をやり直してください。");
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        }).finally(() => document.getElementById("customloader_sub").style.display = "none");
    }

    /**
     * ファイルダウンロード
     * @param {string} apiのpath
     * @param {object} 対象ファイル情報
     */
    output(path, file) {
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
                anchor.download = file.uploadFileName;
                anchor.click();
            })
            .catch(error => {
                console.error('処理に失敗しました', error);
                alert('処理に失敗しました');
            });
    }

    //回答通知処理
    nextAnswerNotificationView() {
        const notificable = this.state.notificable;
        const status = this.state.status;
        if (!notificable) {
            alert("回答通知権限がありません。");
            return false;
        }
        fetch(Config.config.apiUrl + "/answer/notification", {
            method: 'POST',
            body: JSON.stringify(this.state.ApplyAnswerForm),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
            .then(res => {
                if (res.status === 200) {
                    fetch(Config.config.apiUrl + "/label/1003")
                        .then(res => res.json())
                        .then(res => {
                            if (Object.keys(res).length > 0) {
                                this.props.viewState.setCustomMessage(res[0]?.labels?.title, res[0]?.labels?.content)
                                this.props.viewState.nextAnswerNotificationView();
                            } else {
                                alert("labelの取得に失敗しました。");
                            }
                        }).catch(error => {
                            console.error('通信処理に失敗しました', error);
                            alert('通信処理に失敗しました');
                        });
                } else {
                    alert('回答通知に失敗しました');
                }
            }).catch(error => {
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
        const applicationId = this.state.applicationId;
        let lotNumberResult = {};
        let departmentName = [];
        Object.keys(lotNumber).map(key => {
            if (!lotNumberResult[lotNumber[key].districtName]) {
                lotNumberResult[lotNumber[key].districtName] = new Array();
            }
            lotNumberResult[lotNumber[key].districtName].push(lotNumber[key].chiban);
        });
        Object.keys(answers).map(key => {
            if (departmentName.indexOf(answers[key].judgementInformation?.department?.departmentName) < 0) {
                departmentName.push(answers[key].judgementInformation?.department?.departmentName);
            }
        });
        return (
            <>
            <div style={{ position: "absolute", left: -99999 + "px" }} id="refreshConfirmApplicationDetails" onClick={evt => {
                evt.preventDefault();
                evt.stopPropagation();
                this.refresh();
            }}></div>
            <Box
                displayInlineBlock
                backgroundColor={this.props.theme.textLight}
                styledWidth={"700px"}
                styledHeight={"600px"}
                fullHeight
                id="ApplicationDetails"
                onClick={() =>
                    this.props.viewState.setTopElement("ApplicationDetails")}
                css={`
          position: fixed;
          z-index: 9992;
        `}
                className={CustomStyle.custom_frame}
            >
                <div id="customloader_sub" className={CustomStyle.customloaderParent}>
                    <div className={CustomStyle.customloader}>Loading...</div>
                </div>
                <nav className={CustomStyle.custom_nuv} id="ApplicationDetailsDrag">
                    申請情報詳細
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
                            this.props.viewState.hideApplicationDetailsView();
                        }}
                    >
                        <span>戻る</span>
                    </button>
                    <Spacing bottom={3} />

                    {applicationId && (
                        <div className={CustomStyle.box}>
                            <div className={CustomStyle.item} style={{ width: 45 + "%" }}>
                                <button
                                    className={CustomStyle.action_button}
                                    onClick={e => {
                                        this.props.viewState.nextAnswerInputView();
                                    }}
                                >
                                    <span>回答登録</span>
                                </button>
                            </div>
                            <div className={CustomStyle.item} style={{ width: 45 + "%" }}>
                                <button
                                    className={CustomStyle.action_button}
                                    onClick={e => {
                                        this.nextAnswerNotificationView();
                                    }}
                                >
                                    <span>回答通知</span>
                                </button>
                            </div>
                        </div>
                    )}
                    <Spacing bottom={2} />
                    <div className={CustomStyle.scrollContainer}>
                        <div className={CustomStyle.box}>
                            <div className={CustomStyle.item} style={{ width: 60 + "%" }}>
                                ■回答担当課
                            </div>
                            <div className={CustomStyle.item} style={{ width: 40 + "%" }}>
                                {
                                    departmentName.join(",")
                                }
                            </div>
                        </div>
                        <div className={CustomStyle.box}>
                            <div className={CustomStyle.item} style={{ width: 60 + "%" }}>
                                ■ステータス
                            </div>
                            <div className={CustomStyle.item} style={{ width: 40 + "%" }}>
                                {Common.status[status]}
                            </div>
                        </div>
                        <p>■申請者情報</p>
                        {Object.keys(applicantInformation).map(key => (
                            <div className={CustomStyle.box}>
                                <div className={CustomStyle.item} style={{ width: 60 + "%" }}>
                                    ・{applicantInformation[key]?.name}
                                </div>
                                <div className={CustomStyle.item} style={{ width: 40 + "%" }}>
                                    {applicantInformation[key]?.value}
                                </div>
                            </div>
                        ))}
                        {Object.keys(checkedApplicationCategory).map(key => (
                            <div className={CustomStyle.box}>
                                <div className={CustomStyle.item} style={{ width: 60 + "%" }}>
                                    ■{checkedApplicationCategory[key]?.title}
                                </div>
                                <div className={CustomStyle.item} style={{ width: 40 + "%" }}>
                                    {checkedApplicationCategory[key]?.applicationCategory?.map(function (value) { return value.content }).join(",")}
                                </div>
                            </div>
                        ))}
                        <div className={CustomStyle.box}>
                            <div className={CustomStyle.item} style={{ width: 60 + "%" }}>
                                ■申請地番
                            </div>
                            <div className={CustomStyle.item} style={{ width: 40 + "%" }}>
                                {Object.keys(lotNumberResult).map(key => (
                                    <p>{key} {lotNumberResult[key].map(chiban => { return chiban }).join(",")}</p>
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
                                {Object.keys(applicationFile).map(index => (
                                    Object.keys(applicationFile[index]["uploadFileFormList"]).map(key => (
                                        applicationFile[index]["uploadFileFormList"][key].applicationFileId === "9999" && (
                                        <tr>
                                            <td>
                                                <button
                                                    className={CustomStyle.download_button}
                                                    onClick={e => {
                                                        this.output("/application/file/download", applicationFile[index]["uploadFileFormList"][key]);
                                                    }}
                                                >
                                                    <span>ダウンロード</span>
                                                </button>
                                            </td>
                                            <td>
                                                {applicationFile[index]?.applicationFileName}
                                            </td>
                                            <td>xlsx</td>
                                            <td>
                                                {applicationFile[index]["uploadFileFormList"][key]?.uploadFileName}
                                            </td>
                                        </tr>
                                        )
                                    ))
                                ))}
                                {Object.keys(applicationFile).map(index => (
                                    Object.keys(applicationFile[index]["uploadFileFormList"]).map(key => (
                                        applicationFile[index]["uploadFileFormList"][key].applicationFileId !== "9999" && (
                                        <tr>
                                            <td>
                                                <button
                                                    className={CustomStyle.download_button}
                                                    onClick={e => {
                                                        this.output("/application/file/download", applicationFile[index]["uploadFileFormList"][key]);
                                                    }}
                                                >
                                                    <span>ダウンロード</span>
                                                </button>
                                            </td>
                                            <td>
                                                {applicationFile[index]?.applicationFileName}
                                            </td>
                                            <td>pdf</td>
                                            <td>
                                                {applicationFile[index]["uploadFileFormList"][key]?.uploadFileName}
                                            </td>
                                        </tr>
                                        )
                                    ))
                                ))}
                            </tbody>
                        </table>
                    </div>
                    <Spacing bottom={3} />
                </Box>
            </Box >
            </>
        );
    }
}
export default withTranslation()(withTheme(ApplicationDetails));