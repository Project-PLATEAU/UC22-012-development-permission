import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import Button, { RawButton } from "../../../../Styled/Button";
import CustomStyle from "./scss/answer-input.scss";
import Config from "../../../../../customconfig.json";
/**
 * 回答入力画面
 */
@observer
class AnswerInput extends React.Component {

    static displayName = "AnswerInput";

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
            //回答
            answers: [],
            //回答ファイルアップロード対象
            uploadFileFormList: [],
            //回答ファイル削除対象
            deleteFileFormList: {}
        };
    }

    componentDidMount() {
        if (this.props.viewState.applicationInformationSearchForApplicationId) {
            fetch(Config.config.apiUrl + "/application/detail/" + this.props.viewState.applicationInformationSearchForApplicationId)
                .then(res => res.json())
                .then(res => {
                    if (res.applicationId) {
                        Object.keys(res.answers).map(key => {
                            res.answers[key]["uploadFileFormList"] = [];
                        })
                        this.setState({
                            answers: res.answers
                        });
                    } else {
                        alert("回答の取得に失敗しました。再度操作をやり直してください。");
                    }
                }).catch(error => {
                    console.error('通信処理に失敗しました', error);
                    alert('通信処理に失敗しました');
                }).finally(() => document.getElementById("customloader_sub_").style.display = "none");
        } else {
            alert("回答の取得に失敗しました。再度操作をやり直してください。");
            document.getElementById("customloader_sub_").style.display = "none";
        }
        this.draggable(document.getElementById('answerFrameDrag'), document.getElementById('answerFrame'));
    }

    /**
     * アップロード処理
     * @param {event} event
     * @param {number} 対象回答のkey
     */
    fileUpload(event, key) {
        const answers = this.state.answers;
        let files = event.target.files;
        if (files[0].size > 10485760) {
            alert("10M以下のファイルをアップロードできます");
            return false;
        };
        let reader = new FileReader();
        reader.readAsDataURL(files[0]);
        reader.onload = (e) => {
            answers[key]["uploadFileFormList"] = answers[key]["uploadFileFormList"].filter((uploadAnswerFile) => uploadAnswerFile.answerFileName !== files[0].name);
            answers[key]["uploadFileFormList"].push({ "answerFileId": "", "answerId": answers[key].answerId, "answerFileName": files[0].name, "filePath": files[0].name, "uploadFile": files[0] });
            this.setState({
                answers: answers,
            });
        }
    }

    /**
     * 更新対象ファイル全削除
     * @param {number} 対象回答のkey
     */
    targetFilesAllDelete(key) {
        let answers = this.state.answers;
        let deleteFileFormList = this.state.deleteFileFormList;
        answers[key]["uploadFileFormList"] = [];
        deleteFileFormList[key] = [];
        this.setState({
            answers: answers,
            deleteFileFormList: deleteFileFormList
        });
    }

    /**
     * 回答内容入力
     * @param {number} 対象回答のkey
     * @param {string} 入力された値
     */
    inputChange(key, value) {
        let answers = this.state.answers;
        answers[key]["answerContent"] = value;
        this.setState({
            answers: answers,
        });
    }

    // 回答登録
    register() {
        document.getElementById("answerFrame").scrollTop = 0;
        document.getElementById("customloader_sub_").style.display = "block";
        const answers = this.state.answers;
        let deleteFileFormList = this.state.deleteFileFormList;
        let answersContentOnly = [];
        let uploadFileFormList = [];
        let deleteFileFormListCount = 0;
        let notifyCount = 0;
        if (Object.keys(deleteFileFormList).length > 0) {
            Object.keys(deleteFileFormList).map(key => {
                Object.keys(deleteFileFormList[key]).map(fileKey => {
                    if(deleteFileFormList[key][fileKey] && deleteFileFormList[key][fileKey].answerId){
                        deleteFileFormListCount = deleteFileFormListCount + 1;
                    }
                })
            })
        }
        Object.keys(answers).map(key => {
            if (answers[key]["editable"]) {
                answersContentOnly.push({
                    "answerId": answers[key]["answerId"],
                    "editable": answers[key]["editable"],
                    "judgementResult": answers[key]["judgementResult"],
                    "answerContent": answers[key]["answerContent"],
                    "updateDatetime": answers[key]["updateDatetime"],
                    "CompleteFlag": answers[key]["CompleteFlag"],
                    "judgementInformation": answers[key]["judgementInformation"],
                    "answerFiles": answers[key]["answerFiles"]
                })
                Object.keys(answers[key]["uploadFileFormList"]).map(filekey => {
                    if(answers[key]["uploadFileFormList"][filekey] && answers[key]["uploadFileFormList"][filekey].answerId){
                        uploadFileFormList.push(answers[key]["uploadFileFormList"][filekey]);
                    }
                })
            }
        })
        fetch(Config.config.apiUrl + "/answer/input", {
            method: 'POST',
            body: JSON.stringify(answersContentOnly),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
            .then(res => res.json())
            .then(res => {
                if (res.status === 201) {
                    //ファイル削除
                    if (deleteFileFormListCount > 0) {
                        Object.keys(deleteFileFormList).map(key => {
                            Object.keys(deleteFileFormList[key]).map(fileKey => {
                                fetch(Config.config.apiUrl + "/answer/file/delete", {
                                    method: 'POST',
                                    body: JSON.stringify(deleteFileFormList[key][fileKey]),
                                    headers: new Headers({ 'Content-type': 'application/json' }),
                                })
                                    .then(res => res.json())
                                    .then(res => {
                                        deleteFileFormList[key][fileKey]["status"] = res.status;
                                        let completeFlg = true;
                                        Object.keys(deleteFileFormList).map(innerKey => {
                                            Object.keys(deleteFileFormList[innerKey]).map(innerFileKey => {
                                                if (!deleteFileFormList[innerKey][innerFileKey].status) {
                                                    completeFlg = false;
                                                }
                                            })
                                        })
                                        if (completeFlg) {
                                            this.uploadAnswerFile(uploadFileFormList,notifyCount);
                                        }
                                    }).catch(error => {
                                        document.getElementById("customloader_sub_").style.display = "none";
                                        console.error('処理に失敗しました', error);
                                        alert('回答ファイルの削除処理に失敗しました');
                                    });
                            })
                        })
                    } else {
                        this.uploadAnswerFile(uploadFileFormList,notifyCount);
                    }
                } else {
                    document.getElementById("customloader_sub_").style.display = "none";
                    alert('回答登録に失敗しました。一度画面を閉じて再度入力を行い実行してください。');
                }
            }).catch(error => {
                document.getElementById("customloader_sub_").style.display = "none";
                console.error('処理に失敗しました', error);
                alert('処理に失敗しました');
            });
    }

    /**
     * 回答ファイル登録
     * @param {object} アップロード対象の回答ファイル一覧
     * @param {number} 通知処理呼び出し回数
     */
    uploadAnswerFile(uploadFileFormList,notifyCount){
        //ファイルアップロード
        if (Object.keys(uploadFileFormList).length > 0) {
            Object.keys(uploadFileFormList).map(key => {
                const formData = new FormData();
                for (const name in uploadFileFormList[key]) {
                    formData.append(name, uploadFileFormList[key][name]);
                }
                fetch(Config.config.apiUrl + "/answer/file/upload", {
                    method: 'POST',
                    body: formData,
                })
                    .then(res => res.json())
                    .then(res => {
                        uploadFileFormList[key]["status"] = res.status;
                        if (res.status !== 201) {
                            alert('回答ファイルのアップロードに失敗しました');
                        }
                        let completeFlg = true;
                        Object.keys(uploadFileFormList).map(key => {
                            if (!uploadFileFormList[key]["status"]) {
                                completeFlg = false;
                            }
                        })
                        if (completeFlg) {
                            notifyCount = notifyCount + 1;
                            this.notify(notifyCount);
                        }
                    }).catch(error => {
                        document.getElementById("customloader_sub_").style.display = "none";
                        console.error('処理に失敗しました', error);
                        alert('回答ファイルのアップロード処理に失敗しました');
                    });
            })
        } else {
          notifyCount = notifyCount + 1;
          this.notify(notifyCount);  
        }
    }

    /**
     *通知処理
     * @param {number} 通知処理呼び出し回数
     */
    notify(notifyCount){
        if(notifyCount === 1){
            fetch(Config.config.apiUrl + "/label/1002")
            .then(res => res.json())
            .then(res => {
                if (Object.keys(res).length > 0) {
                    this.props.viewState.setCustomMessage(res[0]?.labels?.title, res[0]?.labels?.content)
                    document.getElementById("customloader_sub_").style.display = "none";
                    if(document.getElementById("refreshConfirmApplicationDetails")){
                        document.getElementById("refreshConfirmApplicationDetails").click();
                    }
                    this.props.viewState.nextAnswerCompleteView();
                } else {
                    document.getElementById("customloader_sub_").style.display = "none";
                    alert("labelの取得に失敗しました。");
                }
            }).catch(error => {
                document.getElementById("customloader_sub_").style.display = "none";
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
            });
        }
    }

    /**
     *削除ファイル一覧表示
     * @param {string} entityのid名
     */
    showDeleteAnswerFiles(idName) {
        if (document.getElementById(idName)) {
            document.getElementById(idName).style.display = "block";
        }
    }

    /**
     *削除ファイル一覧非表示
     * @param {string} entityのid名
     */
    closeDeleteAnswerFiles(idName) {
        if (document.getElementById(idName)) {
            document.getElementById(idName).style.display = "none";
        }
    }

    /**
     *ファイルの追加・解除
     * @param {number} 対象回答のkey
     * @param {object} 対象回答ファイル情報
     */
    toggleAnswerFile(key, answerFile) {
        let deleteFileFormList = this.state.deleteFileFormList;
        if (!deleteFileFormList[key]) {
            deleteFileFormList[key] = [];
        }
        const index = deleteFileFormList[key].findIndex(obj =>
            obj.answerFileId == answerFile.answerFileId
        );
        if (index < 0) {
            deleteFileFormList[key].push(answerFile);
        } else {
            deleteFileFormList[key].splice(index, 1);
        }
        this.setState({ deleteFileFormList: deleteFileFormList });
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
        const answers = this.state.answers;
        const deleteFileFormList = this.state.deleteFileFormList;
        return (
            <Box
                displayInlineBlock
                backgroundColor={this.props.theme.textLight}
                styledWidth={"700px"}
                styledHeight={"600px"}
                fullHeight
                onClick={() =>
                    this.props.viewState.setTopElement("AnswerInput")}
                css={`
          position: fixed;
          z-index: 9992;
        `}
                className={CustomStyle.custom_frame}
                id="answerFrame"
            >
                <div id="customloader_sub_" className={CustomStyle.customloaderParent}>
                    <div className={CustomStyle.customloader}>Loading...</div>
                </div>
                <nav className={CustomStyle.custom_nuv} id="answerFrameDrag">
                    回答入力
                </nav>
                <Box
                    centered
                    paddedHorizontally={5}
                    paddedVertically={10}
                    displayInlineBlock
                    className={CustomStyle.custom_content}
                >
                    <Spacing bottom={1} />
                    <p className={CustomStyle.explanation}>回答を入力してください。</p>
                    <Spacing bottom={3} />
                    <div className={CustomStyle.scrollContainer}>
                        <table className={CustomStyle.selection_table}>
                            <thead>
                                <tr className={CustomStyle.table_header}>
                                    <th style={{ width: 200 + "px" }}>対象</th>
                                    <th style={{ width: 200 + "px" }}>判定結果</th>
                                    <th style={{ width: 300 + "px" }}>回答内容</th>
                                </tr>
                            </thead>
                            <tbody>
                                {Object.keys(answers).map(key => (
                                    <tr>
                                        <td>
                                            {answers[key]?.judgementInformation?.title}
                                        </td>
                                        <td>
                                            {answers[key]?.judgementResult}
                                        </td>
                                        <td>
                                            {answers[key]?.editable && (
                                                <textarea style={{
                                                    background: "rgba(0,0,0,0.15)",
                                                    border: "none",
                                                    borderRadius: 2 + "px",
                                                    width: 97 + "%",
                                                    resize: "none",
                                                    border: "2px solid black"
                                                }} rows="5" type="text" placeholder="" value={answers[key]?.answerContent}
                                                    autoComplete="off"
                                                    onChange={e => this.inputChange(key, e.target.value)}
                                                ></textarea>
                                            )}
                                            {!answers[key]?.editable && (
                                                answers[key]?.answerContent
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                        <Spacing bottom={3} />
                        <table className={CustomStyle.selection_table}>
                            <thead>
                                <tr className={CustomStyle.table_header}>
                                    <th style={{ width: 80 + "px" }}>担当課</th>
                                    <th style={{ width: 150 + "px" }}>対象</th>
                                    <th style={{ width: 50 + "px" }}>拡張子</th>
                                    <th style={{ width: 100 + "px" }}>アップロード<br />済みファイル</th>
                                    <th style={{ width: 100 + "px" }}>アップロード<br />ファイル</th>
                                    <th style={{ width: 100 + "px" }}>削除対象<br />ファイル</th>
                                    <th style={{ width: 170 + "px" }}></th>
                                </tr>
                            </thead>
                            <tbody>
                                {Object.keys(answers).map(answersKey => (
                                    <>
                                        <tr>
                                            <td>
                                                {answers[answersKey]?.judgementInformation?.department?.departmentName}
                                            </td>
                                            <td>
                                                {answers[answersKey]?.judgementInformation?.title}
                                            </td>
                                            <td>
                                                pdf
                                            </td>
                                            <td>
                                                {answers[answersKey] && (
                                                    answers[answersKey]["answerFiles"]?.map(answerFile => { return answerFile.answerFileName }).join(",")
                                                )}
                                            </td>
                                            <td>
                                                {answers[answersKey] && (
                                                    answers[answersKey]["uploadFileFormList"]?.map(uploadAnswerFile => { return uploadAnswerFile.answerFileName }).join(",")
                                                )}
                                            </td>
                                            <td>
                                                {deleteFileFormList[answersKey] && (
                                                    deleteFileFormList[answersKey]?.map(deleteFile => { return deleteFile.answerFileName }).join(",")
                                                )}
                                            </td>
                                            <td>
                                                {answers[answersKey]?.editable && (
                                                    <div className={CustomStyle.table_inner_box}>
                                                        <div className={CustomStyle.item}>
                                                            {answers[answersKey]?.uploadFileFormList && Object.keys(answers[answersKey]?.uploadFileFormList).length >= 1 && (
                                                                <>
                                                                    <label className={CustomStyle.add_button_label} tabIndex="0">
                                                                        <input type="file" className={CustomStyle.upload_button} onClick={(e) => { e.target.value = ''; }} onChange={e => { this.fileUpload(e, answersKey) }} accept=".pdf" />
                                                                        追加
                                                                    </label>
                                                                </>
                                                            )}
                                                            {answers[answersKey]?.uploadFileFormList && Object.keys(answers[answersKey]?.uploadFileFormList).length < 1 && (
                                                                <>
                                                                    <label className={CustomStyle.upload_button_label} tabIndex="0">
                                                                        <input type="file" className={CustomStyle.upload_button} onClick={(e) => { e.target.value = ''; }} onChange={e => { this.fileUpload(e, answersKey) }} accept=".pdf" />
                                                                        登録
                                                                    </label>
                                                                </>
                                                            )}
                                                        </div>
                                                        <div className={CustomStyle.item}>
                                                            <button
                                                                className={CustomStyle.uploaded_delete_button}
                                                                onClick={e => {
                                                                    this.showDeleteAnswerFiles("answerDeleteContainer" + answersKey);
                                                                }}
                                                            >
                                                                <span>削除</span>
                                                            </button>
                                                        </div>
                                                        <div className={CustomStyle.item}>
                                                            <button
                                                                className={CustomStyle.upload_delete_button}
                                                                onClick={e => {
                                                                    this.targetFilesAllDelete(answersKey)
                                                                }}
                                                            >
                                                                <span>解除</span>
                                                            </button>
                                                        </div>
                                                    </div>
                                                )}
                                                {answers[answersKey] && Object.keys(answers[answersKey]["answerFiles"]).length > 0 && (
                                                    <div className={CustomStyle.answer_delete_container} id={"answerDeleteContainer" + answersKey}>
                                                        <Box position="absolute" paddedRatio={3} topRight>
                                                            <RawButton onClick={() => {
                                                                this.closeDeleteAnswerFiles("answerDeleteContainer" + answersKey);
                                                            }}>
                                                                <StyledIcon
                                                                    styledWidth={"16px"}
                                                                    fillColor={this.props.theme.textLight}
                                                                    opacity={"0.5"}
                                                                    glyph={Icon.GLYPHS.closeLight}
                                                                    css={`
                                                                cursor:pointer;
                                                                fill: #000;
                                                            `}
                                                                />
                                                            </RawButton>
                                                        </Box>
                                                        <div className={CustomStyle.answer_delete_container_inner}>
                                                            {Object.keys(answers[answersKey]["answerFiles"]).map(answerFileKey => (
                                                                <div className={CustomStyle.table_inner_box}>
                                                                    <div className={CustomStyle.item} style={{ width: 50 + "%" }}>
                                                                        {answers[answersKey]["answerFiles"][answerFileKey]?.answerFileName}
                                                                    </div>
                                                                    <div className={CustomStyle.item}>
                                                                        {(!deleteFileFormList[answersKey] || !deleteFileFormList[answersKey]?.find((v) => v.answerFileId === answers[answersKey]["answerFiles"][answerFileKey].answerFileId)) && (
                                                                            <button
                                                                                className={CustomStyle.uploaded_delete_add_button}
                                                                                onClick={e => {
                                                                                    this.toggleAnswerFile(answersKey, answers[answersKey]["answerFiles"][answerFileKey]);
                                                                                }}
                                                                            >
                                                                                <span>追加</span>
                                                                            </button>
                                                                        )}
                                                                        {deleteFileFormList[answersKey]?.find((v) => v.answerFileId === answers[answersKey]["answerFiles"][answerFileKey].answerFileId) && (
                                                                            <button
                                                                                className={CustomStyle.upload_delete_button}
                                                                                onClick={e => {
                                                                                    this.toggleAnswerFile(answersKey, answers[answersKey]["answerFiles"][answerFileKey]);
                                                                                }}
                                                                            >
                                                                                <span>解除</span>
                                                                            </button>
                                                                        )}
                                                                    </div>
                                                                </div>
                                                            ))}
                                                        </div>
                                                    </div>
                                                )}
                                            </td>
                                        </tr>
                                    </>
                                ))}
                            </tbody>
                        </table>
                    </div>
                    <Spacing bottom={3} />
                    <div className={CustomStyle.box}>
                        <div className={CustomStyle.item} style={{ width: 48 + "%" }}>
                            <button
                                className={CustomStyle.next_button}
                                onClick={e => {
                                    this.register();
                                }}
                            >
                                <span>登録</span>
                            </button>
                        </div>
                        <div className={CustomStyle.item} style={{ width: 48 + "%" }}>
                            <button
                                className={CustomStyle.back_button}
                                onClick={e => {
                                    this.props.viewState.hideAnswerInputView();
                                }}
                            >
                                <span>キャンセル</span>
                            </button>
                        </div>
                    </div>
                </Box>
            </Box >
        );
    }
}
export default withTranslation()(withTheme(AnswerInput));