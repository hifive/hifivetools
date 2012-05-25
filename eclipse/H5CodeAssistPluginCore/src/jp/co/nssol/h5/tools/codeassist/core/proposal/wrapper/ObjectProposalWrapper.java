/*
 * Copyright (C) 2012 NS Solutions Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package jp.co.nssol.h5.tools.codeassist.core.proposal.wrapper;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.wst.jsdt.core.IJavaScriptElement;
import org.eclipse.wst.jsdt.internal.ui.text.html.HTMLPrinter;
import org.eclipse.wst.jsdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.wst.jsdt.ui.PreferenceConstants;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposal;

/**
 * オブジェクト用のプロポーザルラッパ.
 * 
 * @author NS Solutions Corporation
 * 
 */
@SuppressWarnings("restriction")
public class ObjectProposalWrapper implements ProposalWrapper {

	/**
	 * プロポーザルの実体.
	 */
	private JavaCompletionProposal proposal;
	/**
	 * 追加ドキュメントCSS.
	 */
	private String fgCSSStyles;
	/**
	 * ヘルプドキュメント.
	 */
	private String helpDocument;

	/**
	 * コンストラクタ.<br>
	 * 
	 * @param proposal プロポーザル.
	 */
	ObjectProposalWrapper(JavaCompletionProposal proposal) {

		this.proposal = proposal;

	}

	/**
	 * コンストラクタ.<br>
	 * 
	 * @param proposal プロポーザル.
	 * @param helpDocument ヘルプドキュメント.
	 */
	ObjectProposalWrapper(JavaCompletionProposal proposal, String helpDocument) {

		this(proposal);
		this.helpDocument = helpDocument;
	}

	@Override
	public final IContextInformation getContextInformation() {

		return proposal.getContextInformation();
	}

	@Override
	public final String getDisplayString() {

		return proposal.getDisplayString();
	}

	@Override
	public final int getReplacementOffset() {

		return proposal.getReplacementOffset();
	}

	@Override
	public final int getReplacementLength() {

		return proposal.getReplacementLength();
	}

	@Override
	public final String getReplacementString() {

		return proposal.getReplacementString();
	}

	@Override
	public final Image getImage() {

		return proposal.getImage();
	}

	@Override
	public final String getAdditionalProposalInfo() {

		if (helpDocument == null) {
			return proposal.getAdditionalProposalInfo();
		}
		StringBuffer buffer = new StringBuffer();
		HTMLPrinter.insertPageProlog(buffer, 0, getCSSStyles());
		buffer.append(helpDocument);
		HTMLPrinter.addPageEpilog(buffer);
		String info = buffer.toString();
		return info;
	}

	@Override
	public IJavaScriptElement getJavaElement() {

		return proposal.getJavaElement();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return proposal.toString();
	}

	@Override
	public IJavaCompletionProposal getRealProp() {

		return proposal;
	}

	@Override
	public void apply(IDocument document, char trigger, int offset) {

		proposal.apply(document, trigger, offset);
	}

	@Override
	public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {

		return proposal.getPrefixCompletionText(document, completionOffset);
	}

	@Override
	public Point getSelection(IDocument document) {

		return proposal.getSelection(document);
	}

	@Override
	public final char[] getTriggerCharacters() {

		return proposal.getTriggerCharacters();
	}

	@Override
	public final int getPrefixCompletionStart(IDocument document, int completionOffset) {

		return proposal.getPrefixCompletionStart(document, completionOffset);
	}

	@Override
	public final void apply(IDocument document) {

		proposal.apply(document);
	}

	@Override
	public final int getRelevance() {

		return proposal.getRelevance();
	}

	@Override
	public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {

		proposal.apply(viewer, trigger, stateMask, offset);
	}

	@Override
	public final int getContextInformationPosition() {

		return proposal.getContextInformationPosition();
	}

	@Override
	public IInformationControlCreator getInformationControlCreator() {

		return proposal.getInformationControlCreator();
	}

	@Override
	public int hashCode() {

		return proposal.hashCode();
	}

	@Override
	public boolean isValidFor(IDocument document, int offset) {

		return proposal.isValidFor(document, offset);
	}

	@Override
	public boolean validate(IDocument document, int offset, DocumentEvent event) {

		return proposal.validate(document, offset, event);
	}

	@Override
	public void selected(ITextViewer viewer, boolean smartToggle) {

		proposal.selected(viewer, smartToggle);
	}

	@Override
	public void unselected(ITextViewer viewer) {

		proposal.unselected(viewer);
	}

	/**
	 * Returns the style information for displaying HTML (Javadoc) content.
	 * 
	 * @return the CSS styles
	 * 
	 */
	private String getCSSStyles() {

		if (fgCSSStyles == null) {
			fgCSSStyles = ProposalWrapperUtils.getCSSStyles();
		}
		String css = fgCSSStyles;
		if (css != null) {
			FontData fontData = JFaceResources.getFontRegistry().getFontData(
					PreferenceConstants.APPEARANCE_JAVADOC_FONT)[0];
			css = HTMLPrinter.convertTopLevelFont(css, fontData);
		}
		return css;
	}

}
