package com.example.bakersrecipes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bakersrecipes.ui.theme.BakersRecipesTheme
import com.example.bakersrecipes.ui.theme.Typography

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BakersRecipesTheme {
                BakersRecipeHome()
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun RecipeDetailScreenPreview()
{
    BakersRecipesTheme {
        RecipeDetailScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen()
{
    // A surface container using the 'background' color from the theme
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column() {
            Image(
                painterResource(id = R.drawable.ic_launcher_background),
                "test",
                Modifier
                    .height(248.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )

            Text(
                "Slanac",
                style = Typography.titleLarge,
                modifier = Modifier
                    .padding(
                        top = 16.dp,
                        end = 16.dp,
                        start = 16.dp
                    )
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )


            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 16.dp)
            )
            {
                Card(modifier = Modifier.padding(horizontal = 16.dp))
                {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    )
                    {
                        Text(
                            "Ingredients",
                            style = Typography.titleMedium
                        )

                        RecipeIngredient(
                            name = "test",
                            percent = 1f,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                Card( modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth())
                {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    )
                    {
                        Text(
                            "Make Recipe",
                            style = Typography.titleMedium
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            OutlinedTextField(
                                "",
                                onValueChange = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                label = { Text("Total recipe weight...")},
                                leadingIcon = { Icon(Icons.Filled.Edit, "")}
                            )
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun RecipeIngredient(name:String, percent:Float, modifier: Modifier = Modifier)
{
    Box(modifier = modifier.fillMaxWidth()) {
        Text(
            name,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Text(
            percent.toString(),
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }


}

@Preview(showBackground = true)
@Composable
fun BakersRecipeHomePreview()
{
    BakersRecipesTheme {
        BakersRecipeHome()
    }
}

@Composable
fun BakersRecipeHome()
{
    // A surface container using the 'background' color from the theme
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            item {
                Text(
                    "Baker's Recipes",
                    style = Typography.titleLarge,
                    modifier = Modifier.padding(
                        top = 16.dp,
                        end = 16.dp
                    )
                )
            }
            item {
                RecipeItem("Android", "testname")
            }

            item {
                RecipeItem("Androide", "testnamea")
            }

            item {
                RecipeItem("Androida", "testnamee")
            }

            item {
                RecipeItem("Androida", "testnamee")
            }

            item {
                RecipeItem("Androida", "testnamee")
            }

            item {
                FloatingActionButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Filled.Add, "Add Recipe")
                }
            }
        }
    }
}




@Composable
fun RecipeItem(name:String, username:String, modifier: Modifier = Modifier) {
    Card (
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
        ) {
            Image(
                painterResource(id = R.drawable.ic_launcher_background),
                name,
                Modifier
                    .height(128.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    name,
                    style = Typography.titleMedium
                )
                Text(
                    "by $username",
                    style = Typography.labelSmall
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeItemPreview() {
    BakersRecipesTheme {
        RecipeItem("Android", "username")
    }
}