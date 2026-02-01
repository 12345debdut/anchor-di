#!/usr/bin/env bash
set -e

# Push current branch to the remote. Usage: ./push.sh [branch]
# If no branch given, pushes the current branch.

BRANCH="${1:-$(git branch --show-current)}"
REMOTE="${REMOTE:-origin}"

echo "Pushing branch '$BRANCH' to $REMOTE..."
git push "$REMOTE" "$BRANCH"
echo "Done."
