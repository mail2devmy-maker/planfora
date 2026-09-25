# Add Filter by Location to Asset Manager

Add a horizontal scrollable location filter below the category filter in the Asset Manager screen.

## User Review Required

> [!NOTE]
> The location list will be automatically populated from unique `locationNote` values in the asset database. An "All Locations" option will be provided to clear the filter.

## Proposed Changes

### Assets ViewModel
Add state and logic to handle location filtering.

#### [AssetsViewModel.kt](file:///C:/109Backup/Project/PlanFora/app/src/main/java/com/mail2dev/planfora/ui/assets/AssetsViewModel.kt)

- Add `_selectedLocation` StateFlow and `selectedLocation` public StateFlow.
- Add `availableLocations` StateFlow that derives a sorted list of unique locations from all assets.
- Update `assets` StateFlow to include filtering by `selectedLocation`.
- Add `setLocation(location: String?)` method.

```kotlin
    private val _selectedLocation = MutableStateFlow<String?>(null)
    val selectedLocation: StateFlow<String?> = _selectedLocation

    val availableLocations: StateFlow<List<String>> = repository.getAllAssets()
        .map { assets ->
            assets.map { it.locationNote.ifBlank { "Unassigned" } }
                .distinct()
                .sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setLocation(location: String?) {
        _selectedLocation.value = location
    }
```

### Assets Screen UI
Add the new location filter component and integrate it into the screen layout.

#### [AssetsScreen.kt](file:///C:/109Backup/Project/PlanFora/app/src/main/java/com/mail2dev/planfora/ui/screens/AssetsScreen.kt)

- Implement `LocationFilters` composable using `LazyRow` and `FilterChip`.
- Update `AssetsScreen` to collect location state and render `LocationFilters` below `CategoryFilters`.
- Adjust spacing between filter rows.

```kotlin
@Composable
fun LocationFilters(
    locations: List<String>,
    selectedLocation: String?,
    onLocationSelected: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedLocation == null,
                onClick = { onLocationSelected(null) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Place,
                            contentDescription = null,
                            tint = if (selectedLocation == null) MaterialTheme.colorScheme.onPrimary else Color.Gray,
                            modifier = Modifier.size(16.dp).padding(end = 4.dp)
                        )
                        Text("All")
                    }
                },
                // ... same colors as CategoryFilters
            )
        }
        items(locations) { location ->
            FilterChip(
                selected = selectedLocation == location,
                onClick = { onLocationSelected(location) },
                label = { Text(location) },
                // ... same colors as CategoryFilters
            )
        }
    }
}
```

---

## Verification Plan

### Automated Tests
- No new automated tests planned as this is primarily a UI and ViewModel state integration task. Existing tests for `AssetsViewModel` and `PlantAssetDao` will be run to ensure no regressions.
- `gradlew test`

### Manual Verification
1.  **Launch the App**: Open the Asset Manager screen.
2.  **Check Filters**: Verify both Category and Location filters are visible.
3.  **Apply Location Filter**:
    *   Click a location chip (e.g., "NURSERY").
    *   Verify only assets in that location are shown.
    *   Verify the grouping still works correctly (only one location header should be visible).
4.  **Combine Filters**:
    *   Select a category (e.g., "Tree") AND a location (e.g., "NURSERY").
    *   Verify only Trees in NURSERY are shown.
5.  **Clear Filter**:
    *   Click "All" in the location filter.
    *   Verify all locations are shown again (still respecting category/search filters).
6.  **Edge Case**: Check "Unassigned" location filter if assets with blank location exist.
