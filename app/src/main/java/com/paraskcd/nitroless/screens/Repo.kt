package com.paraskcd.nitroless.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paraskcd.nitroless.components.Container
import com.paraskcd.nitroless.components.DarkContainerPill
import com.paraskcd.nitroless.components.TopBarRepo
import com.paraskcd.nitroless.model.FrequentlyUsedEmotesTable
import com.paraskcd.nitroless.model.RepoTable
import com.paraskcd.nitroless.utils.NetworkImage
import com.paraskcd.nitroless.viewmodel.RepoViewModel

@Composable
fun Repo(animateDrawer: Dp, openDrawer: () -> Unit, closeDrawer: () -> Unit, viewModel: RepoViewModel, closeRepo: () -> Unit) {
    val selectedRepo = viewModel.selectedRepo.observeAsState().value
    val clipboardManager = LocalClipboardManager.current
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Check out this awesome Nitroless Repo - \n${selectedRepo!!.name}\n${selectedRepo.url}")
        putExtra(Intent.EXTRA_TITLE, selectedRepo.name)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    val context = LocalContext.current

    var refreshCount by remember {
        mutableStateOf(1)
    }

    LaunchedEffect(key1 = refreshCount) {
        viewModel.getReposData()
    }

    if (selectedRepo != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.primary)
        ) {
            TopBarRepo(
                titleName = null,
                buttonIcon = Icons.Filled.Menu,
                onNavButtonClicked = { openDrawer() },
                onShareButtonClicked = { context.startActivity(shareIntent) },
                onRepoDeleteButtonClicked = {
                    viewModel.deleteRepo(repo = RepoTable(id = selectedRepo.id!!, repoURL = selectedRepo.url!!))
                    refreshCount++
                    closeRepo()
                }
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colors.background)
                    .offset(
                        x = animateDrawer
                    )
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            if (delta < 0) {
                                closeDrawer()
                            } else {
                                if (delta > 10) {
                                    openDrawer()
                                }
                            }
                        }
                    )
            ) {
                DarkContainerPill {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            NetworkImage(
                                imageURL = selectedRepo.url + selectedRepo.icon,
                                imageDescription = selectedRepo.name,
                                size = 50.dp,
                                shape = CircleShape
                            )
                            Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                                Text(text = selectedRepo.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                if (selectedRepo.author != null) {
                                    Text(text = "By ${selectedRepo.author}", fontWeight = FontWeight.Light, fontSize = 12.sp)
                                }

                            }
                        }
                        Text(text = "${selectedRepo.emotes.size} Emotes", fontWeight = FontWeight.Light, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                    }
                }
                Container {
                    LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 64.dp)) {
                        items(selectedRepo.emotes) { emote ->
                            IconButton(
                                onClick = {
                                    val emoteURL = selectedRepo.url + selectedRepo.path + '/' + emote.name + "." + emote.type
                                    viewModel.addFrequentlyUsedEmote(
                                        emote = FrequentlyUsedEmotesTable(emoteURL = emoteURL)
                                    )
                                    clipboardManager.setText(AnnotatedString(emoteURL))
                                    Toast.makeText(context, "Copied Emote - ${emote.name}", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                NetworkImage(imageURL = selectedRepo.url + selectedRepo.path + '/' + emote.name + "." + emote.type, imageDescription = emote.name, size = 48.dp, shape = RoundedCornerShape(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }

}