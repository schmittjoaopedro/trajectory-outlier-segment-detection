data1 = read.csv(text = "TRAJ,PTS,CT,ALG
100,14803,1319364654,TODS
200,30100,4311549009,TODS
300,44389,10050247035,TODS
400,59578,17282485004,TODS
500,74186,25699498571,TODS
")
#500,74583,26748506381,TODS
#600,89575,42104197850,TODS
#700,105017,70864372247,TODS
#800,119601,76880083682,TODS
#900,135293,112523515366,TODS
#1000,149515,131351729462,TODS


data2 = read.csv(text = "TRAJ,PTS,CT,ALG
100,14941,1485364904,RNNSearch
200,30144,5072645379,RNNSearch
300,45458,23783383109,RNNSearch
400,60027,50749077056,RNNSearch
500,75090,171310321194,RNNSearch
")
#500,74558,154756386818,RNNSearch

data3 = read.csv(text = "TRAJ,PTS,CT,ALG
100,14816,1719564988,CoverTree
200,30106,4045243610,CoverTree
300,45626,30049766590,CoverTree
400,60930,49314268070,CoverTree
500,75382,255656832339,CoverTree
")

data = rbind(data1, data2, data3)
data$CT <- data$CT / 10^9

library(ggplot2)
gg <- ggplot(data, aes(TRAJ, CT, fill = ALG))
gg <- gg + geom_bar(position="dodge",stat="identity")
gg <- gg + geom_text(aes(label = paste(round(CT,2),"s", sep = "")), position = position_dodge(width=80), vjust=-0.25, size = 3.5)
gg <- gg + theme(text = element_text(size=14), title = element_text(size = 14), axis.text = element_text(size = 14))
gg <- gg + xlab("Trajectories number")
gg <- gg + ylab("Time (seconds)")
gg <- gg + scale_fill_discrete(name = "Algorithms")
gg <- gg + ggtitle("Comparison between DBScan indexes")
gg


#temp <- data.frame(y = data$CT, x = data$PTS)
#temp$y <- temp$y / 10^9
#plot(temp$x, temp$y)
#glm1 = glm(temp$y ~ temp$x, family = "poisson")
#lines(temp$x, glm1$fitted.values, col = "black", lwd = 3)

#temp2 <- data.frame(y = data2$CT, x = data2$PTS)
#temp2$y <- temp2$y / 10^9
#points(temp2$x, temp2$y, col = "blue")
#glm2 = glm(temp2$y ~ temp2$x, family = "poisson")
#lines(temp2$x, glm2$fitted.values, col = "blue", lwd = 3)