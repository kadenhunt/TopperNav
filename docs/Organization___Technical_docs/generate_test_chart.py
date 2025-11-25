"""
Generate test execution time bar chart from JUnit test results
"""
import matplotlib.pyplot as plt
import numpy as np

# Test data from actual test runs (in milliseconds)
test_names = [
    'GeoUtils:\ndistance_short',
    'GeoUtils:\nbearing_cardinal',
    'GeoUtils:\ndistance_zero',
    'Search:\nvalidation',
    'Search:\nlong_query',
    'Search:\nrepo_results',
    'Search:\nvery_long'
]

# Times in milliseconds (converted from seconds)
times_ms = [1.0, 0.0, 1.0, 2.0, 0.0, 7.0, 0.0]

# Create figure
fig, ax = plt.subplots(figsize=(10, 6))

# Create bar chart
bars = ax.bar(range(len(test_names)), times_ms, color='#2E86AB', alpha=0.8, edgecolor='black')

# Highlight bars that are non-zero
for i, (bar, time) in enumerate(zip(bars, times_ms)):
    if time > 0:
        bar.set_color('#06A77D')
    else:
        bar.set_color('#D9D9D9')

# Add value labels on top of bars
for i, (bar, time) in enumerate(zip(bars, times_ms)):
    height = bar.get_height()
    label = f'{time:.1f}ms' if time > 0 else '<0.5ms'
    ax.text(bar.get_x() + bar.get_width()/2., height,
            label,
            ha='center', va='bottom', fontsize=9, fontweight='bold')

# Customize plot
ax.set_xlabel('Test Cases', fontsize=12, fontweight='bold')
ax.set_ylabel('Execution Time (milliseconds)', fontsize=12, fontweight='bold')
ax.set_title('Unit Test Execution Times - TopperNav\nAll Tests Passed (7/7)', 
             fontsize=14, fontweight='bold', pad=20)
ax.set_xticks(range(len(test_names)))
ax.set_xticklabels(test_names, rotation=45, ha='right', fontsize=9)

# Add grid for readability
ax.grid(axis='y', alpha=0.3, linestyle='--')
ax.set_axisbelow(True)

# Set y-axis limit to zoom in on actual test times (0-15ms range)
ax.set_ylim(0, 15)

# Add annotation about 200ms target instead of line
ax.text(0.98, 0.65, '200ms UI Target ✓\n(All tests well below)', 
        transform=ax.transAxes, fontsize=10, ha='right',
        bbox=dict(boxstyle='round', facecolor='lightcoral', alpha=0.3))

# Remove the legend line since we removed the axhline
# ax.legend(loc='upper right', fontsize=10)

# Add summary text box
summary_text = f'Total Tests: 7\nPassed: 7 (100%)\nMax Time: 7.0ms\nAll < 200ms target ✓'
props = dict(boxstyle='round', facecolor='lightgreen', alpha=0.3)
ax.text(0.02, 0.98, summary_text, transform=ax.transAxes, fontsize=10,
        verticalalignment='top', bbox=props, family='monospace')

plt.tight_layout()

# Save figure
output_path = 'images/test_execution_times.png'
plt.savefig(output_path, dpi=300, bbox_inches='tight')
print(f"Chart saved to: {output_path}")

# Also save as PDF for LaTeX
output_pdf = 'images/test_execution_times.pdf'
plt.savefig(output_pdf, bbox_inches='tight')
print(f"PDF saved to: {output_pdf}")

plt.show()
