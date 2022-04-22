# 关于DialogFragment
DialogFragment继承自Fragment，因此生命周期同Fragment。  
想要自定义界面按照全屏尺寸显示，需要在xml根目录使用一层界面，width和height都为match_parent。
此时显示的dialog中背景默认为白色，如果想设置透明，需要在onCreateView中执行
```
dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
```
执行完之后背景为透明