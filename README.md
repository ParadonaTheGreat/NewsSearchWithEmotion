# NewsSearchWithEmotion

## How to Use

This app is a news aggregator and bias detector. Users can either search up a topic in the search bar, or press enter with nothing in the search bar to load all articles. The app then shows the user the top 400 articles on The Guardian, their topic, and the emotion shown in the article. A neutral emotion would show an article with no bias, while the other emotions would show the leanings of the author. The user can click the articles in the list to read them. 

## Project

This was made in Android Studio. This app uses The Guardian API to fetch articles. It then runs each article through a Hugging Face model (https://huggingface.co/michellejieli/emotion_text_classifier) to find the emotion used in the article. The app then formats the results and adds them to a ListView. Clicking each item brings the user to another activity. 


