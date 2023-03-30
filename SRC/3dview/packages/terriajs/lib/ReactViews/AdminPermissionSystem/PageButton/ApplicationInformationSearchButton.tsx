import React from "react";
import { useTranslation } from "react-i18next";

import ViewState from "../../../ReactViewModels/ViewState";
import Icon from "../../../Styled/Icon";

import Styles from "../../Map/HelpButton/help-button.scss";

interface Props {
  viewState: ViewState;
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
          props.viewState.showApplicationInformationSearchView();
        }}
      >
        <Icon glyph={Icon.GLYPHS.apply} />
        <span>申請情報検索</span>
      </button>
    </div>
  );
};
