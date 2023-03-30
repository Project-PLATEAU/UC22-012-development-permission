import React from "react";
import { useTranslation } from "react-i18next";

import ViewState from "../../../ReactViewModels/ViewState";
import Icon from "../../../Styled/Icon";

import Styles from "../../Map/HelpButton/help-button.scss";
import Terria from "../../../Models/Terria";


interface Props {
  viewState: ViewState;
  terria: Terria;
}

export default (props: Props) => {
  const { t } = useTranslation();

  return (
    <div>
      <button
        className={Styles.helpBtn}
        onClick={evt => {
          evt.preventDefault();
          evt.stopPropagation();
          window.location.href = "./login/";
        }}
      >
        <Icon glyph={Icon.GLYPHS.user} />
        {props.terria.authorityJudgment() && 
            <span>行政ログアウト</span>
        }
        {!props.terria.authorityJudgment() && 
            <span>行政ログイン</span>
        }
      </button>
    </div>
  );
};
