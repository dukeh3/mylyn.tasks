/*******************************************************************************
 * Copyright (c) 2004, 2015 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Jingwen Ou - comment grouping
 *******************************************************************************/

package org.eclipse.mylyn.internal.tasks.ui.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.ui.FillWidthLayout;
import org.eclipse.mylyn.commons.ui.SelectionProviderAdapter;
import org.eclipse.mylyn.commons.ui.compatibility.CommonColors;
import org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil;
import org.eclipse.mylyn.commons.workbench.forms.ScalingHyperlink;
import org.eclipse.mylyn.internal.tasks.core.TaskComment;
import org.eclipse.mylyn.internal.tasks.ui.actions.CommentActionGroup;
import org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy.CommentGroup;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Robert Elves
 * @author Steffen Pingel
 * @author Jingwen Ou
 */
public class TaskEditorCommentPart extends AbstractTaskEditorPart {

	private static final String ID_POPUP_MENU = "org.eclipse.mylyn.tasks.ui.editor.menu.comments"; //$NON-NLS-1$

	public class CommentGroupViewer {

		private final CommentGroup commentGroup;

		private ArrayList<CommentViewer> commentViewers;

		private Section groupSection;

		private boolean renderedInSubSection;

		public CommentGroupViewer(CommentGroup commentGroup) {
			this.commentGroup = commentGroup;
		}

		private Composite createCommentViewers(Composite parent, FormToolkit toolkit) {
			List<CommentViewer> viewers = getCommentViewers();
			Composite composite = toolkit.createComposite(parent);

			GridLayout contentLayout = new GridLayout();
			contentLayout.marginHeight = 0;
			contentLayout.marginWidth = 0;
			composite.setLayout(contentLayout);

			for (CommentViewer commentViewer : viewers) {
				Control control = commentViewer.createControl(composite, toolkit);
				GridDataFactory.fillDefaults().grab(true, false).applyTo(control);
			}
			return composite;
		}

		private Control createControl(final Composite parent, final FormToolkit toolkit) {
			if (renderedInSubSection) {
				return createSection(parent, toolkit);
			} else {
				if (TaskEditorCommentPart.this.commentAttributes.size() >= CommentGroupStrategy.MAX_CURRENT) {
					// show a separator before current comments
					final Canvas separator = new Canvas(parent, SWT.NONE) {
						@Override
						public Point computeSize(int wHint, int hHint, boolean changed) {
							return new Point((wHint == SWT.DEFAULT) ? 1 : wHint, 1);
						}
					};
					separator.addPaintListener(new PaintListener() {
						public void paintControl(PaintEvent e) {
							e.gc.setForeground(separator.getForeground());
							e.gc.drawLine(0, 0, separator.getSize().x, 0);
						}
					});
					separator.setForeground(toolkit.getColors().getColor(IFormColors.TB_BORDER));
					GridDataFactory.fillDefaults().grab(true, false).indent(2 * INDENT, 0).applyTo(separator);
				}
				return createCommentViewers(parent, toolkit);
			}
		}

		private Section createSection(final Composite parent, final FormToolkit toolkit) {
			int style = ExpandableComposite.TWISTIE | ExpandableComposite.SHORT_TITLE_BAR
					| ExpandableComposite.LEFT_TEXT_CLIENT_ALIGNMENT;
//			if (/*commentGroup.hasIncoming() || */expandAllInProgress) {
//				style |= ExpandableComposite.EXPANDED;
//			}

			groupSection = toolkit.createSection(parent, style);
			groupSection.clientVerticalSpacing = 0;
			if (commentGroup.hasIncoming()) {
				groupSection.setBackground(getTaskEditorPage().getAttributeEditorToolkit().getColorIncoming());
			}
			groupSection.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
			groupSection.setText(commentGroup.getGroupName() + Messages.TaskEditorCommentPart_0
					+ commentGroup.getCommentAttributes().size() + Messages.TaskEditorCommentPart_1);

			if (groupSection.isExpanded()) {
				Composite composite = createCommentViewers(groupSection, toolkit);
				groupSection.setClient(composite);
			} else {
				groupSection.addExpansionListener(new ExpansionAdapter() {
					@Override
					public void expansionStateChanged(ExpansionEvent e) {
						if (commentGroup.hasIncoming()) {
							if (e.getState()) {
								groupSection.setBackground(null);
							} else {
								// only decorate background with incoming color when collapsed, otherwise 
								// there is too much decoration in the editor
								groupSection.setBackground(getTaskEditorPage().getAttributeEditorToolkit()
										.getColorIncoming());
							}
						}
						if (groupSection.getClient() == null) {
							try {
								getTaskEditorPage().setReflow(false);
								Composite composite = createCommentViewers(groupSection, toolkit);
								groupSection.setClient(composite);
							} finally {
								getTaskEditorPage().setReflow(true);
							}
							reflow();
						}
					}
				});
			}

			return groupSection;
		}

		public List<CommentViewer> getCommentViewers() {
			if (commentViewers != null) {
				return commentViewers;
			}

			commentViewers = new ArrayList<CommentViewer>(commentGroup.getCommentAttributes().size());
			for (final TaskAttribute commentAttribute : commentGroup.getCommentAttributes()) {
				CommentViewer commentViewer = new CommentViewer(commentAttribute);
				commentViewers.add(commentViewer);
			}
			return commentViewers;
		}

		public boolean isExpanded() {
			if (groupSection != null) {
				return groupSection.isExpanded();
			}

			if (commentViewers != null) {
				for (CommentViewer commentViewer : commentViewers) {
					if (commentViewer.isExpanded()) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * Returns true if this group and all comments in it are expanded.
		 */
		public boolean isFullyExpanded() {
			if (groupSection != null && !groupSection.isExpanded()) {
				return false;
			}
			if (commentViewers != null) {
				for (CommentViewer commentViewer : commentViewers) {
					if (!commentViewer.isExpanded()) {
						return false;
					}
				}
				return true;
			}
			return false;
		}

		public boolean isRenderedInSubSection() {
			return renderedInSubSection;
		}

		public void setExpanded(boolean expanded) {
			if (groupSection != null && groupSection.isExpanded() != expanded) {
				CommonFormUtil.setExpanded(groupSection, expanded);
			}
		}

		/**
		 * Expands this group and all comments in it.
		 */
		public void setFullyExpanded(boolean expanded) {
			if (groupSection != null && groupSection.isExpanded() != expanded) {
				CommonFormUtil.setExpanded(groupSection, expanded);
			}

			if (commentViewers != null) {
				for (CommentViewer commentViewer : commentViewers) {
					commentViewer.setExpanded(expanded);
				}
			}
		}

		public void setRenderedInSubSection(boolean renderedInSubSection) {
			this.renderedInSubSection = renderedInSubSection;
		}

		public void createSectionHyperlink(String message, HyperlinkAdapter listener) {
			if (groupSection != null) {
				ScalingHyperlink resultLink = new ScalingHyperlink(groupSection, SWT.READ_ONLY);
				resultLink.setForeground(CommonColors.HYPERLINK_WIDGET);
				resultLink.setUnderlined(true);
				resultLink.setText(message);
				groupSection.setTextClient(resultLink);
				resultLink.getParent().layout(true, true);
				resultLink.addHyperlinkListener(listener);
			}
		}

		public void clearSectionHyperlink() {
			if (groupSection != null) {
				groupSection.setTextClient(null);
			}
		}

		//		private void createToolBar(final FormToolkit toolkit) {
//		if (section == null) {
//			return;
//		}
//
//		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
//
//		Action collapseAllAction = new Action("") {
//			@Override
//			public void run() {
//				toggleSection(section, false);
//			}
//		};
//		collapseAllAction.setImageDescriptor(CommonImages.COLLAPSE_ALL_SMALL);
//		collapseAllAction.setToolTipText("Collapse All Current Comments");
//		toolBarManager.add(collapseAllAction);
//
//		Action expandAllAction = new Action("") {
//			@Override
//			public void run() {
//				toggleSection(section, true);
//			}
//		};
//		expandAllAction.setImageDescriptor(CommonImages.EXPAND_ALL_SMALL);
//		expandAllAction.setToolTipText("Expand All Current Comments");
//		toolBarManager.add(expandAllAction);
//
//		Composite toolbarComposite = toolkit.createComposite(section);
//		toolbarComposite.setBackground(null);
//		RowLayout rowLayout = new RowLayout();
//		rowLayout.marginTop = 0;
//		rowLayout.marginBottom = 0;
//		rowLayout.marginLeft = 0;
//		rowLayout.marginRight = 0;
//		toolbarComposite.setLayout(rowLayout);
//
//		toolBarManager.createControl(toolbarComposite);
//		section.setTextClient(toolbarComposite);
//	}

	}

	public class CommentViewer {

		private Composite buttonComposite;

		private final TaskAttribute commentAttribute;

		private ExpandableComposite commentComposite;

		private final TaskComment taskComment;

		private AbstractAttributeEditor editor;

		private boolean suppressSelectionChanged;

		public CommentViewer(TaskAttribute commentAttribute) {
			this.commentAttribute = commentAttribute;
			this.taskComment = new TaskComment(getModel().getTaskRepository(), getModel().getTask(), commentAttribute);
		}

		public Control createControl(Composite composite, final FormToolkit toolkit) {
			boolean hasIncomingChanges = getModel().hasIncomingChanges(commentAttribute);
			getTaskData().getAttributeMapper().updateTaskComment(taskComment, commentAttribute);
			int style = ExpandableComposite.TREE_NODE | ExpandableComposite.LEFT_TEXT_CLIENT_ALIGNMENT
					| ExpandableComposite.COMPACT;
			if (hasIncomingChanges || (expandAllInProgress && !suppressExpandViewers)) {
				style |= ExpandableComposite.EXPANDED;
			}
			commentComposite = toolkit.createExpandableComposite(composite, style);
			commentComposite.clientVerticalSpacing = 0;
			commentComposite.setLayout(new GridLayout());
			commentComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			commentComposite.setTitleBarForeground(toolkit.getColors().getColor(IFormColors.TITLE));

			buttonComposite = createTitle(commentComposite, toolkit);

			final Composite commentTextComposite = toolkit.createComposite(commentComposite);
			commentComposite.setClient(commentTextComposite);
			commentTextComposite.setLayout(new FillWidthLayout(EditorUtil.getLayoutAdvisor(getTaskEditorPage()), 15, 0,
					0, 3));
			commentComposite.addExpansionListener(new ExpansionAdapter() {
				@Override
				public void expansionStateChanged(ExpansionEvent event) {
					expandComment(toolkit, commentTextComposite, event.getState());
				}
			});
			if (hasIncomingChanges) {
				commentComposite.setBackground(getTaskEditorPage().getAttributeEditorToolkit().getColorIncoming());
			}
			if (commentComposite.isExpanded()) {
				expandComment(toolkit, commentTextComposite, true);
			}

			// for outline
			EditorUtil.setMarker(commentComposite, commentAttribute.getId());
			return commentComposite;
		}

		private Composite createTitle(final ExpandableComposite commentComposite, final FormToolkit toolkit) {
			// always visible
			Composite titleComposite = toolkit.createComposite(commentComposite);
			commentComposite.setTextClient(titleComposite);
			RowLayout rowLayout = new RowLayout();
			rowLayout.pack = true;
			rowLayout.marginLeft = 0;
			rowLayout.marginBottom = 0;
			rowLayout.marginTop = 0;
			EditorUtil.center(rowLayout);
			titleComposite.setLayout(rowLayout);
			titleComposite.setBackground(null);

			ImageHyperlink expandCommentHyperlink = createTitleHyperLink(toolkit, titleComposite, taskComment);
			expandCommentHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {
					CommonFormUtil.setExpanded(commentComposite, !commentComposite.isExpanded());
				}
			});

			ToolBarManager toolBarManagerTitle = new ToolBarManager(SWT.FLAT);
			addActionsToToolbarTitle(toolBarManagerTitle, taskComment, this);
			toolBarManagerTitle.createControl(titleComposite);

			// only visible when section is expanded
			final Composite buttonComposite = toolkit.createComposite(titleComposite);
			RowLayout buttonCompLayout = new RowLayout();
			buttonCompLayout.marginBottom = 0;
			buttonCompLayout.marginTop = 0;
			buttonComposite.setLayout(buttonCompLayout);
			buttonComposite.setBackground(null);
			buttonComposite.setVisible(commentComposite.isExpanded());

			ToolBarManager toolBarManagerButton = new ToolBarManager(SWT.FLAT);
			addActionsToToolbarButton(toolBarManagerButton, taskComment, this);
			toolBarManagerButton.createControl(buttonComposite);

			return buttonComposite;
		}

		private ImageHyperlink createTitleHyperLink(final FormToolkit toolkit, final Composite toolbarComp,
				final ITaskComment taskComment) {
			ImageHyperlink formHyperlink = toolkit.createImageHyperlink(toolbarComp, SWT.NONE);
			formHyperlink.setBackground(null);
			formHyperlink.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
			IRepositoryPerson author = taskComment.getAuthor();
			if (author != null && author.matchesUsername(getTaskEditorPage().getTaskRepository().getUserName())) {
				formHyperlink.setImage(CommonImages.getImage(CommonImages.PERSON_ME_NARROW));
			} else {
				formHyperlink.setImage(CommonImages.getImage(CommonImages.PERSON_NARROW));
			}
			StringBuilder sb = new StringBuilder();
			if (taskComment.getNumber() >= 0) {
				sb.append(taskComment.getNumber());
				sb.append(": "); //$NON-NLS-1$
			}
			String toolTipText = ""; //$NON-NLS-1$;
			if (author != null) {
				if (author.getName() != null) {
					sb.append(author.getName());
					toolTipText = author.getPersonId();
				} else {
					sb.append(author.getPersonId());
				}
			}
			if (taskComment.getCreationDate() != null) {
				sb.append(", "); //$NON-NLS-1$
				sb.append(EditorUtil.formatDateTime(taskComment.getCreationDate()));
			}
			formHyperlink.setFont(commentComposite.getFont());
			formHyperlink.setToolTipText(toolTipText);
			formHyperlink.setText(sb.toString());
			formHyperlink.setEnabled(true);
			formHyperlink.setUnderlined(false);
			return formHyperlink;
		}

		private void expandComment(FormToolkit toolkit, Composite composite, boolean expanded) {
			buttonComposite.setVisible(expanded);
			if (expanded && composite.getData(KEY_EDITOR) == null) {
				// create viewer
				TaskAttribute textAttribute = getTaskData().getAttributeMapper().getAssoctiatedAttribute(
						taskComment.getTaskAttribute());
				editor = createAttributeEditor(textAttribute);
				if (editor != null) {
					editor.setDecorationEnabled(false);
					editor.createControl(composite, toolkit);
					editor.getControl().addMouseListener(new MouseAdapter() {
						@Override
						public void mouseDown(MouseEvent e) {
							getTaskEditorPage().selectionChanged(taskComment);
						}
					});
					composite.setData(KEY_EDITOR, editor);

					getTaskEditorPage().getAttributeEditorToolkit().adapt(editor);
					reflow();
				}
			} else if (!expanded && composite.getData(KEY_EDITOR) != null) {
				// dispose viewer
				AbstractAttributeEditor editor = (AbstractAttributeEditor) composite.getData(KEY_EDITOR);
				editor.getControl().setMenu(null);
				editor.getControl().dispose();
				composite.setData(KEY_EDITOR, null);
				reflow();
			}
			if (!suppressSelectionChanged) {
				getTaskEditorPage().selectionChanged(taskComment);
			}
		}

		public boolean isExpanded() {
			return commentComposite != null && commentComposite.isExpanded();
		}

		public void setExpanded(boolean expanded) {
			if (commentComposite != null && commentComposite.isExpanded() != expanded) {
				CommonFormUtil.setExpanded(commentComposite, expanded);
			}
		}

		/**
		 * Returns the comment viewer.
		 * 
		 * @return null, if the viewer has not been constructed
		 */
		public AbstractAttributeEditor getEditor() {
			return editor;
		}

		public TaskAttribute getTaskAttribute() {
			return commentAttribute;
		}

		public TaskComment getTaskComment() {
			return taskComment;
		}

		public Control getControl() {
			return commentComposite;
		}

		public void suppressSelectionChanged(boolean value) {
			this.suppressSelectionChanged = value;
		}

	}

	private class ReplyToCommentAction extends AbstractReplyToCommentAction implements IMenuCreator {

		private final ITaskComment taskComment;

		private final CommentViewer commentViewer;

		public ReplyToCommentAction(CommentViewer commentViewer, ITaskComment taskComment) {
			super(TaskEditorCommentPart.this.getTaskEditorPage(), taskComment);
			this.commentViewer = commentViewer;
			this.taskComment = taskComment;
			setMenuCreator(this);
		}

		@Override
		protected String getReplyText() {
			return taskComment.getText();
		}

		public Menu getMenu(Control parent) {
			currentViewer = commentViewer;
			selectionProvider.setSelection(new StructuredSelection(taskComment));
			return commentMenu;
		}

		public void dispose() {
		}

		public Menu getMenu(Menu parent) {
			selectionProvider.setSelection(new StructuredSelection(taskComment));
			return commentMenu;
		}

	}

	/** Expandable composites are indented by 6 pixels by default. */
	private static final int INDENT = -6;

	private static final String KEY_EDITOR = "viewer"; //$NON-NLS-1$

	private List<TaskAttribute> commentAttributes;

	private CommentGroupStrategy commentGroupStrategy;

	private List<CommentGroupViewer> commentGroupViewers;

	private boolean expandAllInProgress;

	private boolean hasIncoming;

	/**
	 * We can't use the reflow flag in AbstractTaskEditorPage because it gets set at various points where we might not
	 * want to reflow.
	 */
	private boolean reflow = true;

	protected Section section;

	private SelectionProviderAdapter selectionProvider;

	// XXX: stores a reference to the viewer for which the commentMenu was displayed last
	private CommentViewer currentViewer;

	private Menu commentMenu;

	private CommentActionGroup actionGroup;

	private boolean suppressExpandViewers;

	public TaskEditorCommentPart() {
		this.commentGroupStrategy = new CommentGroupStrategy() {
			@Override
			protected boolean hasIncomingChanges(ITaskComment taskComment) {
				return getModel().hasIncomingChanges(taskComment.getTaskAttribute());
			}
		};
		setPartName(Messages.TaskEditorCommentPart_Comments);
	}

	protected void addActionsToToolbarButton(ToolBarManager toolBarManager, final TaskComment taskComment,
			CommentViewer commentViewer) {
		ReplyToCommentAction replyAction = new ReplyToCommentAction(commentViewer, taskComment);
		replyAction.setImageDescriptor(TasksUiImages.COMMENT_REPLY_SMALL);
		toolBarManager.add(replyAction);
	}

	protected void addActionsToToolbarTitle(ToolBarManager toolBarManager, final TaskComment taskComment,
			CommentViewer commentViewer) {
	}

	private void collapseAllComments() {
		try {
			getTaskEditorPage().setReflow(false);

			@SuppressWarnings("unused")
			boolean collapsed = false;
			List<CommentGroupViewer> viewers = getCommentGroupViewers();
			for (int i = 0; i < viewers.size(); i++) {
				if (viewers.get(i).isExpanded()) {
					viewers.get(i).setFullyExpanded(false);
					collapsed = viewers.get(i).isRenderedInSubSection();
					// bug 280152: collapse all groups
					//break;
				}
			}

			// keep section expanded
//			if (!collapsed && section != null) {
//				CommonFormUtil.setExpanded(section, false);
//			}
		} finally {
			getTaskEditorPage().setReflow(true);
		}
		reflow();
	}

	private TaskComment convertToTaskComment(TaskDataModel taskDataModel, TaskAttribute commentAttribute) {
		TaskComment taskComment = new TaskComment(taskDataModel.getTaskRepository(), taskDataModel.getTask(),
				commentAttribute);
		taskDataModel.getTaskData().getAttributeMapper().updateTaskComment(taskComment, commentAttribute);
		return taskComment;
	}

	@Override
	public void createControl(Composite parent, final FormToolkit toolkit) {
		initialize();

		selectionProvider = new SelectionProviderAdapter();
		actionGroup = new CommentActionGroup();

		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				// get comment and add reply action as first item in the menu
				ISelection selection = selectionProvider.getSelection();
				if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
					Object element = ((IStructuredSelection) selection).getFirstElement();
					if (element instanceof ITaskComment) {
						final ITaskComment comment = (ITaskComment) element;
						AbstractReplyToCommentAction replyAction = new AbstractReplyToCommentAction(
								getTaskEditorPage(), comment) {
							@Override
							protected String getReplyText() {
								return comment.getText();
							}
						};
						manager.add(replyAction);
					}
				}
				actionGroup.setContext(new ActionContext(selectionProvider.getSelection()));
				actionGroup.fillContextMenu(manager);

				if (currentViewer != null && currentViewer.getEditor() instanceof RichTextAttributeEditor) {
					RichTextAttributeEditor editor = (RichTextAttributeEditor) currentViewer.getEditor();
					if (editor.getViewSourceAction().isEnabled()) {
						manager.add(new Separator("planning")); //$NON-NLS-1$
						manager.add(editor.getViewSourceAction());
					}
				}
			}
		});
		getTaskEditorPage().getEditorSite().registerContextMenu(ID_POPUP_MENU, menuManager, selectionProvider, false);
		commentMenu = menuManager.createContextMenu(parent);

		section = createSection(parent, toolkit, hasIncoming);
		section.setText(section.getText() + " (" + commentAttributes.size() + ")"); //$NON-NLS-1$ //$NON-NLS-2$

		if (commentAttributes.isEmpty()) {
			section.setEnabled(false);
		} else {
			if (hasIncoming) {
				expandSection(toolkit, section);
			} else {
				section.addExpansionListener(new ExpansionAdapter() {
					@Override
					public void expansionStateChanged(ExpansionEvent event) {
						if (section.getClient() == null) {
							try {
								expandAllInProgress = true;
								getTaskEditorPage().setReflow(false);

								expandSection(toolkit, section);
							} finally {
								expandAllInProgress = false;
								getTaskEditorPage().setReflow(true);
							}
							reflow();
						}
					}
				});
			}
		}
		setSection(toolkit, section);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (actionGroup != null) {
			actionGroup.dispose();
		}
	}

	public void expandAllComments(boolean expandViewers) {
		try {
			expandAllInProgress = true;
			suppressExpandViewers = !expandViewers;
			getTaskEditorPage().setReflow(false);

			if (section != null) {
				// the expandAllInProgress flag will ensure that comments in top-level groups have been 
				// expanded, no need to expand groups explicitly
				//boolean expandGroups = section.getClient() != null;

				CommonFormUtil.setExpanded(section, true);

				if (expandViewers) {
					List<CommentGroupViewer> groupViewers = getCommentGroupViewers();
					for (int i = groupViewers.size() - 1; i >= 0; i--) {
						if (!groupViewers.get(i).isFullyExpanded()) {
							groupViewers.get(i).setFullyExpanded(true);
						}
					}
				}
			}
		} finally {
			expandAllInProgress = false;
			suppressExpandViewers = false;
			getTaskEditorPage().setReflow(true);
		}
		reflow();
	}

	private void expandSection(final FormToolkit toolkit, final Section section) {
		Composite composite = toolkit.createComposite(section);
		section.setClient(composite);
		composite.setLayout(EditorUtil.createSectionClientLayout());

		List<CommentGroupViewer> viewers = getCommentGroupViewers();
		for (CommentGroupViewer viewer : viewers) {
			Control control = viewer.createControl(composite, toolkit);
			if (viewer.isRenderedInSubSection()) {
				// align twistie of sub-section with section
				GridDataFactory.fillDefaults().grab(true, false).indent(2 * INDENT, 0).applyTo(control);
			} else {
				GridDataFactory.fillDefaults().grab(true, false).indent(INDENT, 0).applyTo(control);
			}
		}
	}

	@Override
	protected void fillToolBar(ToolBarManager barManager) {
		Action collapseAllAction = new Action("") { //$NON-NLS-1$
			@Override
			public void run() {
				collapseAllComments();
			}
		};
		collapseAllAction.setImageDescriptor(CommonImages.COLLAPSE_ALL_SMALL);
		collapseAllAction.setToolTipText(Messages.TaskEditorCommentPart_Collapse_Comments);
		barManager.add(collapseAllAction);

		Action expandAllAction = new Action("") { //$NON-NLS-1$
			@Override
			public void run() {
				expandAllComments(true);
			}
		};
		expandAllAction.setImageDescriptor(CommonImages.EXPAND_ALL_SMALL);
		expandAllAction.setToolTipText(Messages.TaskEditorCommentPart_Expand_Comments);
		barManager.add(expandAllAction);

		if (commentAttributes.isEmpty()) {
			collapseAllAction.setEnabled(false);
			expandAllAction.setEnabled(false);
		}
	}

	public CommentGroupStrategy getCommentGroupStrategy() {
		return commentGroupStrategy;
	}

	public void setCommentGroupStrategy(CommentGroupStrategy commentGroupStrategy) {
		this.commentGroupStrategy = commentGroupStrategy;
	}

	public List<CommentGroupViewer> getCommentGroupViewers() {
		if (commentGroupViewers != null) {
			return commentGroupViewers;
		}

		// group comments
		List<ITaskComment> comments = new ArrayList<ITaskComment>();
		for (TaskAttribute commentAttribute : this.commentAttributes) {
			comments.add(convertToTaskComment(getModel(), commentAttribute));
		}
		String currentPersonId = getModel().getTaskRepository().getUserName();
		List<CommentGroup> commentGroups = getCommentGroupStrategy().groupComments(comments, currentPersonId);

		commentGroupViewers = new ArrayList<CommentGroupViewer>(commentGroups.size());
		if (commentGroups.size() > 0) {
			for (int i = 0; i < commentGroups.size(); i++) {
				CommentGroupViewer viewer = new CommentGroupViewer(commentGroups.get(i));
				boolean isLastGroup = i == commentGroups.size() - 1;
				viewer.setRenderedInSubSection(!isLastGroup);
				commentGroupViewers.add(viewer);
			}
		}
		return commentGroupViewers;
	}

	private void initialize() {
		commentAttributes = getTaskData().getAttributeMapper().getAttributesByType(getTaskData(),
				TaskAttribute.TYPE_COMMENT);
		if (commentAttributes.size() > 0) {
			for (TaskAttribute commentAttribute : commentAttributes) {
				if (getModel().hasIncomingChanges(commentAttribute)) {
					hasIncoming = true;
					break;
				}
			}
		}
	}

	@Override
	public boolean setFormInput(Object input) {
		if (input instanceof String) {
			String text = (String) input;
			if (commentAttributes != null) {
				for (TaskAttribute commentAttribute : commentAttributes) {
					if (text.equals(commentAttribute.getId())) {
						selectReveal(commentAttribute);
					}
				}
			}
		}
		return super.setFormInput(input);
	}

	public CommentViewer selectReveal(TaskAttribute commentAttribute) {
		if (commentAttribute == null) {
			return null;
		}
		expandAllComments(false);
		List<CommentGroupViewer> groupViewers = getCommentGroupViewers();
		for (CommentGroupViewer groupViewer : groupViewers) {
			for (CommentViewer viewer : groupViewer.getCommentViewers()) {
				if (viewer.getTaskAttribute().equals(commentAttribute)) {
					// expand section
					groupViewer.setExpanded(true);

					// EditorUtil is consistent with behavior of outline 
					EditorUtil.reveal(getTaskEditorPage().getManagedForm().getForm(), commentAttribute.getId());
					return viewer;
				}
			}
		}
		return null;
	}

	public boolean isCommentSectionExpanded() {
		return section != null && section.isExpanded();
	}

	public void reflow() {
		if (reflow) {
			getTaskEditorPage().reflow();
		}
	}

	public void setReflow(boolean reflow) {
		this.reflow = reflow;
	}
}
