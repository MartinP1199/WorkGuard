package sk.potociarm.workguard.ui.tags

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import sk.potociarm.workguard.R
import sk.potociarm.workguard.WorkGuardTopAppBar
import sk.potociarm.workguard.data.worktag.WorkTag
import sk.potociarm.workguard.ui.AppViewModelProvider
import sk.potociarm.workguard.ui.navigation.NavDestination
import sk.potociarm.workguard.ui.theme.WorkGuardTheme

object WorkTagEditDestination : NavDestination {
    override val route = "work_tag_edits"
    override val titleRes = R.string.worktag_detail_title
    const val ID_ARG = "id"
    val routeWithArgs = "$route/{$ID_ARG}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkTagEditScreen(
    navigateBack: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkTagEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val tagListState by viewModel.otherTagsUiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    //todo viewmodel val tagUiState = viewModel.uiState.collectAsState()
    //todo uistate val parentTagUiState = viewModel.uiParentState.collectAsState()
    Scaffold(
        topBar = {
            WorkGuardTopAppBar(
                title = stringResource(WorkTagDetailsDestination.titleRes),
                canNavigateBack = true,
                navigateUp = navigateBack
            )
        },
    ) { innerPadding ->
        WorkTagEditBody(
            workTagUiState = viewModel.tagUiState,
            allTag = tagListState.tagList,
            onDelete = {
                coroutineScope.launch {
                    viewModel.deleteTag()
                    onDelete()
                }
            },
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                    top = innerPadding.calculateTopPadding()
                )
                .verticalScroll(rememberScrollState())

        )
    }
}

@Composable
private fun WorkTagEditBody(
    workTagUiState: WorkTagUi,
    allTag: List<WorkTag>,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),

        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        var deleteConfirmationRequired by rememberSaveable { mutableStateOf(false) }

        WorkTagEditCard(
            tag = workTagUiState,
            allTag = allTag,
        )

        OutlinedButton(
            onClick = { deleteConfirmationRequired = true },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.work_tag_delete))
        }
        if (deleteConfirmationRequired) {
            DeleteConfirmationDialog(
                onDeleteConfirm = {
                    deleteConfirmationRequired = false
                    onDelete()
                },
                onDeleteCancel = { deleteConfirmationRequired = false },
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
            )
        }
    }
}

@Composable
fun WorkTagEditCard(
    tag: WorkTagUi,
    allTag: List<WorkTag>,
    modifier: Modifier = Modifier,
    startExpanded: Boolean = false
) {
    OutlinedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(
                id = R.dimen
                    .elevation
            )
        ),
        border = BorderStroke(
            dimensionResource(
                id = R.dimen
                    .borderSize
            ), Color.Black
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {

        Column(
            modifier.padding(all = dimensionResource(R.dimen.padding_medium)),
        ) {

            OutlinedTextField(
                modifier = modifier,
                value = tag.name,
                onValueChange = { tag.name = it },
                label = { Text(stringResource(id = R.string.workTag_name_req)) }
            )

            OutlinedTextField(
                value = tag.price.toString() + " €/h", //todo currency
                onValueChange = { tag.name = it },
                label = { Text(stringResource(id = R.string.tag_price)) }
            )

            //todo dropdown parent tag
            ParentDropdownMenu(tag, allTag)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParentDropdownMenu(
    tag: WorkTagUi,
    allTag: List<WorkTag>,
    startExpanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(startExpanded) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = false },
    ) {
        TextField(
            // The `menuAnchor` modifier must be passed to the text field to handle
            // expanding/collapsing the menu on click. A read-only text field has
            // the anchor type `PrimaryNotEditable`.
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            //readOnly = true,
            label = { Text(stringResource(id = R.string.tag_parent)) },
            onValueChange = {},
            value = allTag.find { it.id == tag.parentId }?.name
                ?: stringResource(id = R.string.no_tag_parent),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            allTag.forEach { currentTag ->
                DropdownMenuItem(
                    //text = { Text(workTag.name) },
                    text = {
                        Text(currentTag.name)
                    },
                    onClick = {
                        tag.parentId = currentTag.id
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun WorkTagEditPreview() {
    WorkTagEditCard(
        tag = sampleTagUiWithParent(),
        allTag = sampleTagList(),
    )
}

@Preview(showBackground = true)
@Composable
fun WorkTagEditWithoutParentPreview() {
    WorkTagEditCard(
        tag = sampleTagUiWithoutParent(),
        allTag = sampleTagList()
    )
}

@Preview(showBackground = true)
@Composable
fun ParentDropdownMenuPreview() {
    ParentDropdownMenu(
        tag = sampleTagUiWithParent(),
        allTag = sampleTagList(),
        startExpanded = true
    )
}

@Preview(showBackground = true)
@Composable
fun WorkTagEditParentPreview() {
    WorkTagEditCard(
        tag = sampleTagUiWithParent(),
        allTag = sampleTagList(),
        startExpanded = true
    )
}




@Preview(showBackground = true)
@Composable
fun WorkEditScreenPreview() {
    WorkGuardTheme {
        WorkTagEditBody(
            sampleTagUiWithoutParent(),
            onDelete = {},
            allTag = sampleTagList(),
        )
    }
}


@Composable
private fun DeleteConfirmationDialog(
    onDeleteConfirm: () -> Unit, onDeleteCancel: () -> Unit, modifier: Modifier = Modifier
) {
    AlertDialog(onDismissRequest = { /* Do nothing */ },
        title = { Text(stringResource(R.string.attention)) },
        text = { Text(stringResource(R.string.delete_question)) },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDeleteCancel) {
                Text(text = stringResource(R.string.no))
            }
        },
        confirmButton = {
            TextButton(onClick = onDeleteConfirm) {
                Text(text = stringResource(R.string.yes))
            }
        })
}

